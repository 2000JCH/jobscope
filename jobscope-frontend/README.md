# JobScope 프론트엔드

React 19 + Vite 기반 모바일 우선 PWA

## 기술 스택

- React 19 / JavaScript
- Zustand (전역 상태 관리)
- FullCalendar (캘린더 UI)
- Axios (HTTP 클라이언트)
- lucide-react (아이콘)
- Pretendard (웹폰트)
- vite-plugin-pwa (PWA)

## 개발 서버 실행

```bash
npm install --legacy-peer-deps
npm run dev
```

> `--legacy-peer-deps` 필요: vite-plugin-pwa가 Vite 8 peer dependency 미지원

## 환경변수

`.env.example` 참고하여 `.env` 파일 생성

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_KAKAO_CLIENT_ID=your_kakao_rest_api_key
VITE_KAKAO_REDIRECT_URI=http://localhost:5173/login
```

## 빌드 및 배포

Docker 기반 배포는 루트 `docker-compose.yml` 사용

```bash
# 루트 디렉토리에서
docker-compose up --build
```

자세한 내용은 루트 [README.md](../README.md) 참고