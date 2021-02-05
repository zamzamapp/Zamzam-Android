package com.zamzam.chat.jobmanager.workmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zamzam.chat.jobs.AttachmentDownloadJob;
import com.zamzam.chat.jobs.AttachmentUploadJob;
import com.zamzam.chat.jobs.AvatarGroupsV1DownloadJob;
import com.zamzam.chat.jobs.CleanPreKeysJob;
import com.zamzam.chat.jobs.CreateSignedPreKeyJob;
import com.zamzam.chat.jobs.DirectoryRefreshJob;
import com.zamzam.chat.jobs.FailingJob;
import com.zamzam.chat.jobs.FcmRefreshJob;
import com.zamzam.chat.jobs.LocalBackupJob;
import com.zamzam.chat.jobs.MmsDownloadJob;
import com.zamzam.chat.jobs.MmsReceiveJob;
import com.zamzam.chat.jobs.MmsSendJob;
import com.zamzam.chat.jobs.MultiDeviceBlockedUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceConfigurationUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceContactUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceGroupUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceProfileKeyUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceReadUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceVerifiedUpdateJob;
import com.zamzam.chat.jobs.PushDecryptMessageJob;
import com.zamzam.chat.jobs.PushGroupSendJob;
import com.zamzam.chat.jobs.PushGroupUpdateJob;
import com.zamzam.chat.jobs.PushMediaSendJob;
import com.zamzam.chat.jobs.PushNotificationReceiveJob;
import com.zamzam.chat.jobs.PushTextSendJob;
import com.zamzam.chat.jobs.RefreshAttributesJob;
import com.zamzam.chat.jobs.RefreshPreKeysJob;
import com.zamzam.chat.jobs.RequestGroupInfoJob;
import com.zamzam.chat.jobs.RetrieveProfileAvatarJob;
import com.zamzam.chat.jobs.RetrieveProfileJob;
import com.zamzam.chat.jobs.RotateCertificateJob;
import com.zamzam.chat.jobs.RotateProfileKeyJob;
import com.zamzam.chat.jobs.RotateSignedPreKeyJob;
import com.zamzam.chat.jobs.SendDeliveryReceiptJob;
import com.zamzam.chat.jobs.SendReadReceiptJob;
import com.zamzam.chat.jobs.ServiceOutageDetectionJob;
import com.zamzam.chat.jobs.SmsReceiveJob;
import com.zamzam.chat.jobs.SmsSendJob;
import com.zamzam.chat.jobs.SmsSentJob;
import com.zamzam.chat.jobs.TrimThreadJob;
import com.zamzam.chat.jobs.TypingSendJob;
import com.zamzam.chat.jobs.UpdateApkJob;

import java.util.HashMap;
import java.util.Map;

public class WorkManagerFactoryMappings {

  private static final Map<String, String> FACTORY_MAP = new HashMap<String, String>() {{
    put("AttachmentDownloadJob", AttachmentDownloadJob.KEY);
    put("AttachmentUploadJob", AttachmentUploadJob.KEY);
    put("AvatarDownloadJob", AvatarGroupsV1DownloadJob.KEY);
    put("CleanPreKeysJob", CleanPreKeysJob.KEY);
    put("CreateSignedPreKeyJob", CreateSignedPreKeyJob.KEY);
    put("DirectoryRefreshJob", DirectoryRefreshJob.KEY);
    put("FcmRefreshJob", FcmRefreshJob.KEY);
    put("LocalBackupJob", LocalBackupJob.KEY);
    put("MmsDownloadJob", MmsDownloadJob.KEY);
    put("MmsReceiveJob", MmsReceiveJob.KEY);
    put("MmsSendJob", MmsSendJob.KEY);
    put("MultiDeviceBlockedUpdateJob", MultiDeviceBlockedUpdateJob.KEY);
    put("MultiDeviceConfigurationUpdateJob", MultiDeviceConfigurationUpdateJob.KEY);
    put("MultiDeviceContactUpdateJob", MultiDeviceContactUpdateJob.KEY);
    put("MultiDeviceGroupUpdateJob", MultiDeviceGroupUpdateJob.KEY);
    put("MultiDeviceProfileKeyUpdateJob", MultiDeviceProfileKeyUpdateJob.KEY);
    put("MultiDeviceReadUpdateJob", MultiDeviceReadUpdateJob.KEY);
    put("MultiDeviceVerifiedUpdateJob", MultiDeviceVerifiedUpdateJob.KEY);
    put("PushContentReceiveJob", FailingJob.KEY);
    put("PushDecryptJob", PushDecryptMessageJob.KEY);
    put("PushGroupSendJob", PushGroupSendJob.KEY);
    put("PushGroupUpdateJob", PushGroupUpdateJob.KEY);
    put("PushMediaSendJob", PushMediaSendJob.KEY);
    put("PushNotificationReceiveJob", PushNotificationReceiveJob.KEY);
    put("PushTextSendJob", PushTextSendJob.KEY);
    put("RefreshAttributesJob", RefreshAttributesJob.KEY);
    put("RefreshPreKeysJob", RefreshPreKeysJob.KEY);
    put("RefreshUnidentifiedDeliveryAbilityJob", FailingJob.KEY);
    put("RequestGroupInfoJob", RequestGroupInfoJob.KEY);
    put("RetrieveProfileAvatarJob", RetrieveProfileAvatarJob.KEY);
    put("RetrieveProfileJob", RetrieveProfileJob.KEY);
    put("RotateCertificateJob", RotateCertificateJob.KEY);
    put("RotateProfileKeyJob", RotateProfileKeyJob.KEY);
    put("RotateSignedPreKeyJob", RotateSignedPreKeyJob.KEY);
    put("SendDeliveryReceiptJob", SendDeliveryReceiptJob.KEY);
    put("SendReadReceiptJob", SendReadReceiptJob.KEY);
    put("ServiceOutageDetectionJob", ServiceOutageDetectionJob.KEY);
    put("SmsReceiveJob", SmsReceiveJob.KEY);
    put("SmsSendJob", SmsSendJob.KEY);
    put("SmsSentJob", SmsSentJob.KEY);
    put("TrimThreadJob", TrimThreadJob.KEY);
    put("TypingSendJob", TypingSendJob.KEY);
    put("UpdateApkJob", UpdateApkJob.KEY);
  }};

  public static @Nullable String getFactoryKey(@NonNull String workManagerClass) {
    return FACTORY_MAP.get(workManagerClass);
  }
}
