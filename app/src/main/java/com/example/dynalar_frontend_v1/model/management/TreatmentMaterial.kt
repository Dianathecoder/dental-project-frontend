package com.example.dynalar_frontend_v1.model.management

import com.example.dynalar_frontend_v1.model.management.Material

data class TreatmentMaterial(val id: Long,
                             val material: Material,
                             val quantityRequired: Int)
