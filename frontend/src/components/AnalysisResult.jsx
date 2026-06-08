"use client";

import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import Image from "next/image";

export function AnalysisResult({ result, image }) {
  const getConfidenceColor = (confidence) => {
    if (confidence >= 90) return "bg-green-100 text-green-800 border-green-200";
    if (confidence >= 70) return "bg-yellow-100 text-yellow-800 border-yellow-200";
    return "bg-red-100 text-red-800 border-red-200";
  };

  const diseaseLibrary = {
    "Early Blight": {
      description:
        "Early blight is a fungal disease that causes dark, concentric spots on older leaves and can reduce yield if untreated.",
      treatment:
        "Remove infected foliage and apply a fungicide labeled for early blight. Avoid overhead watering and improve airflow.",
      preventionTips: [
        "Rotate crops to reduce pathogen buildup",
        "Water at the base to keep foliage dry",
        "Remove plant debris after harvest",
        "Use disease-resistant varieties when available",
      ],
    },
    "Late Blight": {
      description:
        "Late blight is a fast-spreading disease that causes water-soaked lesions and can destroy plants rapidly in cool, wet conditions.",
      treatment:
        "Remove infected plants immediately and apply a suitable fungicide. Avoid irrigation that wets leaves.",
      preventionTips: [
        "Ensure good airflow between plants",
        "Avoid overhead irrigation",
        "Monitor crops frequently during cool, wet weather",
        "Destroy infected plant material promptly",
      ],
    },
    Healthy: {
      description: "No disease detected. Your plant appears to be healthy.",
      treatment: "Continue with regular care and maintenance.",
      preventionTips: [
        "Maintain regular watering schedule",
        "Fertilize as appropriate for the plant species",
        "Monitor for early signs of pests or disease",
        "Ensure adequate sunlight and proper soil conditions",
      ],
    },
    "Powdery Mildew": {
      description:
        "Powdery mildew is a fungal disease that affects a wide range of plants. It appears as a white to gray powdery growth on leaf surfaces, stems, and sometimes fruit.",
      treatment:
        "Apply fungicides specifically labeled for powdery mildew. Improve air circulation around plants and avoid overhead watering.",
      preventionTips: [
        "Space plants properly to improve air circulation",
        "Water at the base of plants to keep foliage dry",
        "Remove and destroy infected plant parts",
        "Use resistant varieties when available",
      ],
    },
    "Leaf Spot": {
      description:
        "Leaf spot is a common plant disease characterized by brown or black spots on leaves. It's caused by various fungi and bacteria.",
      treatment:
        "Remove and destroy infected leaves. Apply appropriate fungicide or bactericide depending on the specific pathogen.",
      preventionTips: [
        "Avoid wetting leaves when watering",
        "Ensure proper spacing between plants",
        "Clean up fallen leaves and plant debris",
        "Rotate crops annually",
      ],
    },
  };

  const diseaseLabel = result?.class ?? result?.disease ?? "Unknown";
  const libraryEntry = diseaseLibrary[diseaseLabel] ?? {};
  const description =
    result?.description ??
    libraryEntry.description ??
    "No description available for this prediction.";
  const treatment =
    result?.treatment ??
    libraryEntry.treatment ??
    "No treatment guidance available.";
  const preventionTips =
    result?.preventionTips ?? libraryEntry.preventionTips ?? [];
  const confidenceValue = typeof result?.confidence === "number" ? result.confidence : 0;
  const confidencePercent =
    confidenceValue <= 1 ? Math.round(confidenceValue * 100) : Math.round(confidenceValue);
  const modelName = result?.model ?? null;
  const processingTime = result?.processing_time_s ?? null;
  const probabilities = result?.probabilities ?? null;

  return (
    <Card className="mt-8 overflow-hidden">
      <div className="md:flex">
        <div className="md:w-1/3">
          <div className="h-64 bg-gray-100">
            {image && (
              <Image
                src={image}
                alt="Analyzed plant"
                width={600}
                height={500}
                className="h-full w-full object-cover"
              />
            )}
          </div>
        </div>
        <div className="p-6 md:w-2/3">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-2xl font-bold text-green-800">
              {diseaseLabel}
            </h3>
            <Badge
              className={`${getConfidenceColor(confidencePercent)} px-3 py-1`}
            >
              {confidencePercent}% Confidence
            </Badge>
          </div>

          {probabilities && (
            <div className="mb-4 space-y-1">
              {Object.entries(probabilities).map(([label, prob]) => (
                <div key={label} className="flex items-center gap-2 text-sm">
                  <span className="w-28 text-gray-600">{label}</span>
                  <div className="h-2 flex-1 rounded-full bg-gray-200">
                    <div
                      className="h-full rounded-full bg-green-500 transition-all"
                      style={{ width: `${Math.round(prob * 100)}%` }}
                    />
                  </div>
                  <span className="w-12 text-right text-gray-500">
                    {(prob * 100).toFixed(1)}%
                  </span>
                </div>
              ))}
            </div>
          )}

          <div className="mb-4 flex gap-4 text-xs text-gray-400">
            {modelName && <span>Model: {modelName}</span>}
            {processingTime && <span>Time: {processingTime.toFixed(2)}s</span>}
          </div>

          <Separator className="my-4" />

          <div className="mb-4">
            <h4 className="mb-2 font-semibold text-gray-700">Description</h4>
            <p className="text-gray-600">{description}</p>
          </div>

          <div className="mb-4">
            <h4 className="mb-2 font-semibold text-gray-700">Treatment</h4>
            <p className="text-gray-600">{treatment}</p>
          </div>

          <div>
            <h4 className="mb-2 font-semibold text-gray-700">
              Prevention Tips
            </h4>
            <ul className="list-inside list-disc text-gray-600">
              {preventionTips.length > 0 ? (
                preventionTips.map((tip, index) => <li key={index}>{tip}</li>)
              ) : (
                <li>No prevention tips available.</li>
              )}
            </ul>
          </div>
        </div>
      </div>
    </Card>
  );
}
