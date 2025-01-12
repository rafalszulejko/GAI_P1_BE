package com.gauntletai.chat.s3;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class S3Attachment {
    private String key;
    private String filename;
}
