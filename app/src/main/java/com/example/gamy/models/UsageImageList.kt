package com.example.gamy.models

import com.google.firebase.firestore.PropertyName
import com.google.j2objc.annotations.Property

data class UsageImageList (
    @PropertyName("images") val images:List<String>?=null
        )
