package com.github.dactiv.saas.config.domain.body;

import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.minio.ObjectItem;
import io.minio.messages.Item;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BucketObjectItem extends ObjectItem {

    @Serial
    private static final long serialVersionUID = 2218170619209440211L;

    private Bucket bucket;

    public BucketObjectItem(Item item) {
        super(item);
    }

    public String getBucket() {
        return bucket.getBucketName();
    }
}
