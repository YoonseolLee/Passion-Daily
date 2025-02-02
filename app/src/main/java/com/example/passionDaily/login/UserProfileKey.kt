package com.example.passionDaily.login

enum class UserProfileKey(val key: String) {
    ID("id"),
    EMAIL("email"),
    ROLE("role"),
    LAST_LOGIN_DATE("lastLoginDate"),
    FCM_TOKEN("fcmToken"),
    NOTIFICATION_ENABLED("notificationEnabled"),
    NOTIFICATION_TIME("notificationTime"),
    PRIVACY_POLICY_ENABLED("privacyPolicyEnabled"),
    TERMS_OF_SERVICE_ENABLED("termsOfServiceEnabled"),
    LAST_SYNC_DATE("lastSyncDate"),
    IS_ACCOUNT_DELETED("isAccountDeleted"),
    CREATED_DATE("createdDate"),
    MODIFIED_DATE("modifiedDate")
}