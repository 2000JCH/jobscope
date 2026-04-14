package com.jobscope.global.alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobscope.domain.alarm.entity.AlarmType;
import com.jobscope.domain.oauth.service.OAuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMessageService {

    private static final String KAKAO_MEMO_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    private final OAuthTokenService oAuthTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 카카오 나에게 보내기로 그룹화된 알림 메시지를 발송한다.
     * 같은 alarmType에 속한 모든 지원건을 stage별로 묶어 1건의 메시지로 발송한다.
     *
     * @param userId           수신 유저 ID
     * @param alarmType        알림 타입 (D7/D3/D1/DEADLINE)
     * @param totalCount       총 알림 건수
     * @param stageToCompanies stage명 → 회사명 목록 (삽입 순서 유지)
     * @return 발송 성공 여부
     */
    public boolean sendMessage(Long userId, AlarmType alarmType, int totalCount,
                               Map<String, List<String>> stageToCompanies) {
        try {
            String accessToken = oAuthTokenService.getValidAccessToken(userId);
            String text = buildMessage(alarmType, totalCount, stageToCompanies);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            Map<String, Object> link = new LinkedHashMap<>();
            link.put("web_url", "");
            link.put("mobile_web_url", "");

            Map<String, Object> templateObject = new LinkedHashMap<>();
            templateObject.put("object_type", "text");
            templateObject.put("text", text);
            templateObject.put("link", link);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("template_object", objectMapper.writeValueAsString(templateObject));

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = restTemplate.postForObject(
                    KAKAO_MEMO_URL, new HttpEntity<>(params, headers), Map.class);

            // NOTE: 카카오는 일부 에러를 200 OK + result_code 음수로 내려줌
            if (responseBody != null && responseBody.containsKey("result_code")) {
                int resultCode = (int) responseBody.get("result_code");
                if (resultCode != 0) {
                    log.error("[KakaoMessageService] 나에게 보내기 발송 실패(result_code: {}) - userId: {}, alarmType: {}",
                            resultCode, userId, alarmType);
                    return false;
                }
            }

            log.info("[KakaoMessageService] 나에게 보내기 발송 완료 - userId: {}, alarmType: {}, count: {}",
                    userId, alarmType, totalCount);
            return true;

        } catch (JsonProcessingException e) {
            log.error("[KakaoMessageService] 메시지 직렬화 실패 - userId: {}, alarmType: {}, error: {}",
                    userId, alarmType, e.getMessage(), e);
            return false;
        } catch (RestClientException e) {
            log.error("[KakaoMessageService] 나에게 보내기 발송 실패 - userId: {}, alarmType: {}, error: {}",
                    userId, alarmType, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[KakaoMessageService] 나에게 보내기 발송 중 예외 - userId: {}, alarmType: {}, error: {}",
                    userId, alarmType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 알림 타입별 그룹화 메시지 텍스트를 생성한다.
     *
     * <pre>
     * [JobScope] D-Day 3건
     * - 서류 마감: 네이버, 카카오
     * - 1차 면접: 삼성전자
     * </pre>
     */
    private String buildMessage(AlarmType alarmType, int totalCount,
                                Map<String, List<String>> stageToCompanies) {
        StringBuilder sb = new StringBuilder();
        sb.append("[JobScope] ").append(toLabel(alarmType)).append(" ").append(totalCount).append("건\n");
        stageToCompanies.forEach((stage, companies) ->
                sb.append("- ").append(stage).append(": ")
                        .append(String.join(", ", companies)).append("\n")
        );
        return sb.toString().stripTrailing();
    }

    private String toLabel(AlarmType alarmType) {
        return switch (alarmType) {
            case D7 -> "D-7";
            case D3 -> "D-3";
            case D1 -> "D-1";
            case DEADLINE -> "D-Day";
        };
    }
}