package com.jobscope.global.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeleteEntity extends BaseEntity {

    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
