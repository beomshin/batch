package com.example.batch.entity.domain;

import com.example.batch.entity.domain.common.BaseEntity;
import com.example.batch.entity.domain.converter.BoardStatusConverter;
import com.example.batch.entity.domain.converter.PostTypeConverter;
import com.example.batch.entity.domain.embedded.ContentEmbedded;
import com.example.batch.entity.domain.enums.BoardStatus;
import com.example.batch.entity.domain.enums.LineType;
import com.example.batch.entity.domain.enums.PostType;
import com.example.batch.entity.domain.enums.WriterType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Slf4j
@Entity(name = "BoardTb")
@Table(name = "BoardTb")
@ToString(callSuper = true)
@Getter
public class BoardTbEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boardId")
    private long id;

    @Embedded
    private ContentEmbedded contentEmbedded; // 내용

    @Column(name = "writeDt")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp writeDt; // 수정일

    @Enumerated(EnumType.ORDINAL) // 순서를 데이터베이스 저장
    @Column(name = "lineType")
    private LineType lineType;

    @Enumerated(EnumType.ORDINAL) // 순서를 데이터베이스 저장
    @Column(name = "writerType")
    private WriterType writerType;

    @Column(name = "postType")
    @Convert(converter = PostTypeConverter.class)
    private PostType postType;

    @Column(name = "status")
    @Convert(converter = BoardStatusConverter.class)
    private BoardStatus status;

    @Column(name = "commentCount")
    private Long commentCount;

    @Column(name = "recommendCount")
    private Long recommendCount;

    @Column(name = "view")
    private Long view;

    @Column(name = "report")
    private Long report;
}
