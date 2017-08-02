package com.bielang.customserver.util;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * realm数据库版本迁移
 * Created by Daylight on 2017/8/2.
 */

public class Migration implements RealmMigration {
    @SuppressWarnings("UnusedAssignment")
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema= realm.getSchema();
            RealmObjectSchema msgListSchema=schema.get("MsgList");
            msgListSchema.transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    obj.set("mHeaderUrl",Integer.class);
                }
            });
            oldVersion++;
    }
}
