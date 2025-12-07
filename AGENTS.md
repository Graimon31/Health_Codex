# Health Codex Agent Notes

## Scope
These instructions apply to the entire repository.

## Build & Test
- Use the Gradle wrapper when available: `./gradlew test` and `./gradlew assembleDebug`.
- In this container the Android SDK may be missing; note that in reports if commands cannot run.

## Neural Forecast Model
- The TensorFlow Lite model is stored at `app/src/main/assets/ml/health_forecast_model.tflite`.
- To retrain/update the model:
  1. Prepare normalized/encoded features as documented in `README.md` (one-hot categorical, min-max numeric).
  2. Train the MLP with the specified layer stack (128-64-32, ReLU, dropout/batch-norm) using Adam and cross-entropy.
  3. Export to `health_forecast_model.tflite` and replace the asset in `app/src/main/assets/ml/`.
- When integrating new models, keep the feature order in `NeuralForecastAnalyzer.buildFeatureVector` aligned with the training pipeline.

## Style
- Keep Kotlin code documented and avoid adding TODOs.
- Do not wrap imports in try/catch.
