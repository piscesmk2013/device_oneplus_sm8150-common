#
# Copyright (C) 2018-2019 The LineageOS Project
# Copyright (C) 2021 The PixelExperience Project
#
# SPDX-License-Identifier: Apache-2.0
#

BUILD_BROKEN_DUP_RULES := true
BUILD_BROKEN_ELF_PREBUILT_PRODUCT_COPY_FILES := true

BOARD_VENDOR := oneplus

VENDOR_PATH := device/oneplus/sm8150-common

# Architecture
TARGET_ARCH := arm64
TARGET_ARCH_VARIANT := armv8-2a-dotprod
TARGET_CPU_ABI := arm64-v8a
TARGET_CPU_VARIANT := cortex-a76
TARGET_CPU_VARIANT_RUNTIME := cortex-a76

ifeq (,$(filter %_64,$(TARGET_PRODUCT)))
TARGET_2ND_ARCH := arm
TARGET_2ND_ARCH_VARIANT := armv8-2a
TARGET_2ND_CPU_ABI := armeabi-v7a
TARGET_2ND_CPU_ABI2 := armeabi
TARGET_2ND_CPU_VARIANT := cortex-a76
TARGET_2ND_CPU_VARIANT_RUNTIME := cortex-a76
endif

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := msmnile
TARGET_NO_BOOTLOADER := true

# Kernel
BOARD_BOOT_HEADER_VERSION := 2
BOARD_KERNEL_BASE := 0x00000000
BOARD_KERNEL_CMDLINE := androidboot.hardware=qcom androidboot.console=ttyMSM0 androidboot.memcg=1 lpm_levels.sleep_disabled=1 msm_rtb.filter=0x237 service_locator.enable=1 swiotlb=2048 loop.max_part=7 androidboot.usbcontroller=a600000.dwc3 kpti=off
BOARD_KERNEL_CMDLINE += androidboot.vbmeta.avb_version=1.0
BOARD_KERNEL_IMAGE_NAME := Image
BOARD_KERNEL_PAGESIZE := 4096
BOARD_KERNEL_SEPARATED_DTBO := true
BOARD_MKBOOTIMG_ARGS += --header_version $(BOARD_BOOT_HEADER_VERSION)
BOARD_RAMDISK_USE_LZ4 := true
TARGET_KERNEL_ADDITIONAL_FLAGS := LLVM=1 LD=ld.lld AS=llvm-as AR=llvm-ar NM=llvm-nm OBJCOPY=llvm-objcopy OBJDUMP=llvm-objdump STRIP=llvm-strip
TARGET_KERNEL_CLANG_COMPILE := true
TARGET_KERNEL_CLANG_VERSION := r487747c
TARGET_KERNEL_CONFIG := gulch_defconfig
TARGET_KERNEL_SOURCE := kernel/oneplus/sm8150

TARGET_KERNEL_ADDITIONAL_FLAGS := \
    HOSTCFLAGS="-fuse-ld=lld -Wno-unused-command-line-argument"

# Platform
TARGET_BOARD_PLATFORM := msmnile

# Properties
TARGET_ODM_PROP += $(VENDOR_PATH)/odm.prop
TARGET_PRODUCT_PROP += $(VENDOR_PATH)/product.prop
TARGET_SYSTEM_PROP += $(VENDOR_PATH)/system.prop
TARGET_SYSTEM_EXT_PROP += $(VENDOR_PATH)/system_ext.prop
TARGET_VENDOR_PROP += $(VENDOR_PATH)/vendor.prop

# A/B
AB_OTA_UPDATER := true

AB_OTA_PARTITIONS += \
    boot \
    dtbo \
    odm \
    system \
    vendor \
    vbmeta

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "qualcomm-hidl"

# Audio
AUDIO_FEATURE_ENABLED_DS2_DOLBY_DAP := true
AUDIO_FEATURE_ENABLED_EXTENDED_COMPRESS_FORMAT := true
AUDIO_FEATURE_ENABLED_INSTANCE_ID := true
AUDIO_FEATURE_ENABLED_PROXY_DEVICE := true
BOARD_SUPPORTS_SOUND_TRIGGER := true
USE_CUSTOM_AUDIO_POLICY := 1

# Camera
TARGET_CAMERA_NEEDS_CLIENT_INFO_LIB := true
TARGET_ALTERNATIVE_FUTEX_WAITERS := true
USE_DEVICE_SPECIFIC_CAMERA := true
MALLOC_SVELTE_FOR_LIBC32 := true

# FOD
TARGET_SURFACEFLINGER_UDFPS_LIB := //hardware/oneplus:libudfps_extension.oneplus

# Display
TARGET_USES_FOD_ZPOS := true

# HIDL
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += $(VENDOR_PATH)/device_framework_matrix.xml
DEVICE_MATRIX_FILE += $(VENDOR_PATH)/compatibility_matrix.xml
DEVICE_MANIFEST_FILE += $(VENDOR_PATH)/manifest.xml
ODM_MANIFEST_FILES += $(VENDOR_PATH)/manifest-qva.xml

# HWUI
HWUI_COMPILE_FOR_PERF := true

# Init
TARGET_INIT_VENDOR_LIB := //$(VENDOR_PATH):libinit_oneplus-sm8150
TARGET_RECOVERY_DEVICE_MODULES := libinit_oneplus-sm8150

# Metadata
BOARD_USES_METADATA_PARTITION := true

# Partitions
BOARD_EROFS_COMPRESSOR := lz4
BOARD_EROFS_PCLUSTER_SIZE := 262144
BOARD_FLASH_BLOCK_SIZE := 262144 # (BOARD_KERNEL_PAGESIZE * 64)
BOARD_ODMIMAGE_FILE_SYSTEM_TYPE := erofs
BOARD_SYSTEMIMAGE_FILE_SYSTEM_TYPE := erofs
BOARD_USERDATAIMAGE_FILE_SYSTEM_TYPE := f2fs
BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE := erofs
TARGET_COPY_OUT_ODM := odm
TARGET_COPY_OUT_VENDOR := vendor

# Qcom/common tree
include $(QCOM_COMMON_PATH)/BoardConfigQcom.mk

# Recovery
BOARD_INCLUDE_DTB_IN_BOOTIMG := true
BOARD_INCLUDE_RECOVERY_DTBO := true
TARGET_RECOVERY_PIXEL_FORMAT := RGBX_8888
TARGET_USERIMAGES_USE_F2FS := true

# Security patch level
VENDOR_SECURITY_PATCH := 2022-08-05

BOARD_VENDOR_SEPOLICY_DIRS += $(VENDOR_PATH)/sepolicy/vendor
PRODUCT_PRIVATE_SEPOLICY_DIRS += $(VENDOR_PATH)/sepolicy/private
PRODUCT_PUBLIC_SEPOLICY_DIRS += $(VENDOR_PATH)/sepolicy/public

# Sensors
SOONG_CONFIG_NAMESPACES += ONEPLUS_MSMNILE_SENSORS
SOONG_CONFIG_ONEPLUS_MSMNILE_SENSORS := ALS_POS_X ALS_POS_Y

# Verified Boot
BOARD_AVB_ENABLE := true
ifneq (user,$(TARGET_BUILD_VARIANT))
BOARD_AVB_MAKE_VBMETA_IMAGE_ARGS += --flags 3
else ifneq (,$(wildcard .android-certs/releasekey.key))
BOARD_AVB_ALGORITHM := SHA256_RSA2048
BOARD_AVB_KEY_PATH := .android-certs/releasekey.key
else
BOARD_AVB_MAKE_VBMETA_IMAGE_ARGS += --flags 3
endif

# WiFi
WIFI_HIDL_UNIFIED_SUPPLICANT_SERVICE_RC_ENTRY := true
