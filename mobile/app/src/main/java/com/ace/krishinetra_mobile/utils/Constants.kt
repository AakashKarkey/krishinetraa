package com.ace.krishinetra_mobile.utils

object Constants {
    const val BASE_URL = "http://10.0.2.2:8000/"
    const val PREDICT_ENDPOINT = "predict"
    const val IMG_SIZE = 224
    const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L

    object Disease {
        val DISEASE_INFO = mapOf(
            "Early Blight" to DiseaseData(
                "Early Blight is a common fungal disease caused by Alternaria solani. It affects potato and tomato plants, primarily targeting older leaves. Symptoms include dark brown spots with concentric rings, often surrounded by a yellow halo.",
                "Apply fungicides containing chlorothalonil or mancozeb at the first sign of infection. Remove and destroy affected leaves. Ensure proper crop rotation with non-solanaceous crops. Maintain adequate plant spacing for airflow.",
                listOf("Practice crop rotation with non-host crops", "Water at the base of plants to keep foliage dry", "Apply preventive fungicides during humid weather", "Remove plant debris after harvest", "Use disease-free certified seeds")
            ),
            "Late Blight" to DiseaseData(
                "Late Blight is a devastating disease caused by Phytophthora infestans, the pathogen responsible for the Irish Potato Famine. It affects both potato and tomato plants, causing water-soaked lesions on leaves that rapidly expand and turn brown or black.",
                "Apply systemic fungicides such as metalaxyl or mefenoxam immediately. Remove and destroy all infected plant material. Consider using resistant potato varieties. In severe cases, harvest early to save the crop.",
                listOf("Use resistant potato varieties", "Apply preventive fungicides during cool, wet weather", "Avoid overhead irrigation", "Destroy volunteer potato plants", "Monitor weather conditions for disease risk", "Ensure proper tuber storage conditions")
            ),
            "Healthy" to DiseaseData(
                "Your plant appears healthy with no signs of disease. Continue good care practices to maintain plant health and prevent future infections.",
                "Continue regular care routine. Apply balanced fertilizer as needed. Maintain consistent watering schedule. Monitor for early signs of stress or pest activity.",
                listOf("Water consistently, avoiding overwatering", "Apply mulch to retain moisture and prevent soil splash", "Fertilize appropriately for the growth stage", "Inspect plants weekly for early signs of disease", "Maintain good air circulation around plants", "Rotate crops annually")
            )
        )
    }
}

data class DiseaseData(
    val description: String,
    val treatment: String,
    val preventionTips: List<String>
)
