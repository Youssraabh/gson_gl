package com.google.gson.protobuf;

import com.google.common.base.CaseFormat;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Extension;
import java.util.HashSet;
import java.util.Set;

public class Builder {
    private ProtoTypeAdapter.EnumSerialization enumSerialization;
    private CaseFormat protoFormat;
    private CaseFormat jsonFormat;
    private Set<Extension<FieldOptions, String>> serializedNameExtensions = new HashSet<>();
    private Set<Extension<EnumValueOptions, String>> serializedEnumValueExtensions = new HashSet<>();

    public Builder(ProtoTypeAdapter.EnumSerialization enumSerialization,
                   CaseFormat protoFormat, CaseFormat jsonFormat) {
        this.enumSerialization = enumSerialization;
        this.protoFormat = protoFormat;
        this.jsonFormat = jsonFormat;
    }

    public Builder setEnumSerialization(ProtoTypeAdapter.EnumSerialization enumSerialization) {
        this.enumSerialization = enumSerialization;
        return this;
    }

    public Builder setFieldNameSerializationFormat(CaseFormat protoFormat, CaseFormat jsonFormat) {
        this.protoFormat = protoFormat;
        this.jsonFormat = jsonFormat;
        return this;
    }

    public Builder addSerializedNameExtension(Extension<FieldOptions, String> serializedNameExtension) {
        this.serializedNameExtensions.add(serializedNameExtension);
        return this;
    }

    public Builder addSerializedEnumValueExtension(Extension<EnumValueOptions, String> serializedEnumValueExtension) {
        this.serializedEnumValueExtensions.add(serializedEnumValueExtension);
        return this;
    }

    public ProtoTypeAdapter build() {
        return new ProtoTypeAdapter(enumSerialization, protoFormat, jsonFormat, serializedNameExtensions, serializedEnumValueExtensions);
    }
}
