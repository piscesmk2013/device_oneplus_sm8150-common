# Copyright (C) 2022 Paranoid Android
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

DEVICE_PATH := device/oneplus/sm8150-common

PRODUCT_SOONG_NAMESPACES += \
    $(DEVICE_PATH)/usb \
    vendor/qcom/opensource/usb/etc

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.accessory.xml \
    frameworks/native/data/etc/android.hardware.usb.host.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.host.xml

PRODUCT_PACKAGES += \
    android.hardware.usb@1.0-service \
    init.qcom.usb.rc \
    init.qcom.usb.sh

PRODUCT_PROPERTY_OVERRIDES += \
    vendor.usb.use_gadget_hal=0 \
    vendor.usb.rndis.func.name=gsi \
    vendor.usb.rmnet.func.name=gsi \
    vendor.usb.rmnet.inst.name=rmnet \
    vendor.usb.dpl.inst.name=dpl \
    vendor.usb.qdss.inst.name=qdss \
    vendor.usb.diag.func.name=diag \
    vendor.usb.use_ffs_mtp=1 \
    sys.usb.mtp.batchcancel=1
