
# PocketSpend 💰

## Private AI Expense Tracker

PocketSpend is a privacy-first Android expense tracker that combines traditional expense tracking, receipt OCR, and on-device AI.

The goal is simple:

> **Understand your spending without sending your financial data to a cloud AI service.**

PocketSpend uses a quantized Qwen2.5 model with `llama.cpp` to perform local AI inference directly on the Android device.

---

## ✨ Features

- 💰 Manual expense tracking
- 📊 Monthly spending calculation
- 🧾 Receipt image preview
- 🔍 On-device receipt text extraction
- 🤖 Local AI analysis
- 🔒 Privacy-focused architecture
- 📱 Android application
- ⚡ Native C++ inference through JNI
- 🧠 Qwen2.5 0.5B Instruct GGUF model
- 🦙 llama.cpp inference engine
- 📦 GGUF quantized model support



## 🧠 Architecture

```text
                         PocketSpend
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
        Expense System    Receipt OCR      Local AI
              │               │               │
              │               ▼               │
              │         Extracted Text        │
              │               │               │
              └───────────────┼───────────────┘
                              │
                              ▼
                         JNI Interface
                              │
                              ▼
                        Native C++
                              │
                              ▼
                         llama.cpp
                              │
                              ▼
                   Qwen2.5 0.5B Instruct
                              │
                              ▼
                         AI Response
                              │
                              ▼
                         Android UI
```

---

# 🔐 Privacy-First Design

Traditional AI applications commonly use:

```text
Android App
     │
     ▼
Internet
     │
     ▼
Cloud API
     │
     ▼
LLM
```

PocketSpend is designed around:

```text
Android App
     │
     ├── Receipt OCR
     │
     ├── Expense Data
     │
     ▼
  Local AI
     │
     ▼
 llama.cpp
     │
     ▼
 Local GGUF Model
```

This means financial information can be processed locally without requiring a cloud AI API.

---

# 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android development |
| Jetpack Compose | User interface |
| C++ | Native AI inference |
| JNI | Kotlin ↔ C++ communication |
| llama.cpp | Local LLM inference |
| GGUF | Model format |
| Qwen2.5 0.5B Instruct | Local language model |
| Android NDK | Native compilation |
| CMake | C++ build system |
| OCR | Receipt text extraction |
| Gradle | Android build system |

---

# 📂 Project Structure

```text
PocketSpend/
│
├── app/
│   │
│   ├── src/
│   │   └── main/
│   │       │
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── chhabinath/
│   │       │           └── pocketspend/
│   │       │               └── MainActivity.kt
│   │       │
│   │       ├── cpp/
│   │       │   ├── include/
│   │       │   │   └── llama.h
│   │       │   │
│   │       │   ├── ggml/
│   │       │   │
│   │       │   ├── includes/
│   │       │   │
│   │       │   ├── CMakeLists.txt
│   │       │   │
│   │       │   └── pocketllm.cpp
│   │       │
│   │       └── assets/
│   │           └── qwen2.5-0.5b-instruct-q4_k_m.gguf
│   │
│   └── build.gradle.kts
│
├── gradle/
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

# 🤖 Local AI Pipeline

```text
Receipt Image
     │
     ▼
OCR
     │
     ▼
Extracted Receipt Text
     │
     ▼
Prompt Construction
     │
     ▼
Kotlin
     │
     ▼
JNI
     │
     ▼
C++
     │
     ▼
llama.cpp
     │
     ▼
Qwen2.5 0.5B
     │
     ▼
Generated Response
     │
     ▼
Android UI
```

---

# 🧩 Native AI Architecture

PocketSpend communicates with the local LLM through JNI.

```text
Kotlin
  │
  │ JNI
  ▼
pocketllm.cpp
  │
  ▼
llama.cpp
  │
  ▼
GGUF Model
```

The native layer handles:

1. Model loading
2. Context initialization
3. Prompt tokenization
4. Token generation
5. Sampling
6. Response generation
7. Returning the response to Kotlin

---

# 📦 Model

PocketSpend currently uses:

```text
qwen2.5-0.5b-instruct-q4_k_m.gguf
```

The model is stored in:

```text
app/src/main/assets/
```

The model uses GGUF quantization to reduce memory requirements and make local inference more practical on Android devices.

---

# 📸 Receipt Workflow

## 1. Select Receipt

The user selects or captures a receipt.

## 2. Preview Receipt

PocketSpend displays the selected receipt.

## 3. Extract Text

The OCR pipeline extracts text from the receipt.

Example:

```text
DMART
Groceries
Nissin Cup Noodles
Protein Bars
Fruit Juice
Total Order Bill
₹2854.00
```

## 4. Analyze With Local AI

The extracted text is passed to the local LLM.

The AI can be used to identify:

- Merchant
- Items
- Amounts
- Categories
- Total amount
- Spending information

---

# 💰 Expense Tracking

Example:

```text
Merchant: DMART
Category: Groceries
Amount: ₹450
Date: 2026-10-04
```

The home screen provides:

```text
October Spending

₹4523.00
```

along with recent expenses.

---

# 📱 Application Screens

## Home Screen

The home screen provides:

- Monthly spending
- Recent expenses
- Scan receipt
- Add expense
- AI analysis

Example:

```text
PocketSpend

Private AI Expense Tracker

October Spending

₹4523.00

[ Scan ] [ Add ] [ AI ]

Recent Expenses
```

---

## Receipt Scanner

```text
Scan Receipt

Receipt Preview

[ Receipt Image ]

[ Extract Text ]

Detected Text

[ OCR Result ]

[ Analyze with Local AI ]
```

---

## Local AI Screen

```text
PocketSpend AI

[ Local AI Response ]
```

All AI inference is intended to run locally using the embedded model.

---

# ⚙️ Requirements

To build PocketSpend, install:

- Android Studio
- Android SDK
- Android NDK
- CMake
- Kotlin
- Gradle

A physical Android device is recommended for testing local LLM inference.

The device should have enough RAM and CPU resources for the selected GGUF model.

---

# 🔧 Setup

## 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/PocketSpend.git
```

```bash
cd PocketSpend
```

---

## 2. Open the Project

Open the project in Android Studio:

```text
PocketSpend/
```

Allow Gradle to finish syncing.

---

## 3. Verify the AI Model

Make sure this file exists:

```text
app/src/main/assets/qwen2.5-0.5b-instruct-q4_k_m.gguf
```

---

## 4. Verify Native Files

The native AI code is located here:

```text
app/src/main/cpp/
```

Important files:

```text
cpp/
├── include/
│   └── llama.h
│
├── ggml/
├── includes/
├── CMakeLists.txt
└── pocketllm.cpp
```

---

# 🏗️ Build

From Android Studio:

```text
Build
→ Make Project
```

Or from PowerShell:

```powershell
.\gradlew assembleDebug
```

---

# 📱 Install on Android

Connect an Android device with USB debugging enabled.

Check the device:

```powershell
adb devices
```

Then install:

```powershell
.\gradlew installDebug
```

---

# 🧪 Testing

## Expense System

```text
✓ Add expense
✓ Display expense
✓ Calculate monthly spending
✓ Display recent expenses
```

## Receipt System

```text
✓ Select receipt
✓ Display receipt
✓ Extract text
✓ Display OCR result
```

## Local AI

```text
✓ Load GGUF model
✓ Initialize llama.cpp
✓ Initialize context
✓ Tokenize prompt
✓ Generate tokens
✓ Return response
✓ Display response
```

---

# 🐛 Troubleshooting

## Failed to Tokenize Prompt

If the application displays:

```text
ERROR: Failed to tokenize prompt
```

check:

1. The GGUF model loaded successfully.
2. The prompt is valid UTF-8.
3. The tokenizer belongs to the loaded model.
4. The prompt does not exceed the context size.
5. JNI passes the correct string.
6. The chat template is compatible with the model.
7. The native llama.cpp API matches the version of `llama.h`.

---

## CMake Errors

Check:

```text
app/src/main/cpp/CMakeLists.txt
```

Make sure the llama.cpp and ggml source files are correctly configured.

---

## Model Not Found

Verify:

```text
app/src/main/assets/qwen2.5-0.5b-instruct-q4_k_m.gguf
```

Then clean and rebuild:

```powershell
.\gradlew clean
.\gradlew assembleDebug
```

---

# 🔒 Privacy Architecture

PocketSpend is designed around local-first processing.

```text
                PocketSpend
                     │
       ┌─────────────┴─────────────┐
       │                           │
 Financial Data                 AI
       │                           │
       └─────────────┬─────────────┘
                     │
                     ▼
                ON DEVICE
                     │
                     ▼
               Local LLM
```

The architecture avoids requiring an external AI API for local inference.

---

# 🚧 Current Status

## Implemented

- [x] Android application
- [x] Expense tracking UI
- [x] Monthly spending calculation
- [x] Recent expense display
- [x] Receipt preview
- [x] OCR text extraction
- [x] GGUF model integration
- [x] llama.cpp integration
- [x] JNI communication
- [x] Local AI screen

## In Progress

- [ ] Improve receipt information extraction
- [ ] Improve AI prompt formatting
- [ ] Structured AI output
- [ ] Automatic expense categorization
- [ ] Spending insights
- [ ] Budget recommendations
- [ ] Local inference optimization
- [ ] Better AI response formatting

---

# 🗺️ Roadmap

## Phase 1 — Expense Tracking

- Manual expense entry
- Expense categories
- Monthly statistics
- Local persistence

## Phase 2 — Receipt Intelligence

- OCR
- Merchant detection
- Amount extraction
- Automatic categorization

## Phase 3 — Local AI

Enable natural-language queries such as:

```text
How much did I spend on groceries this month?
```

```text
What was my biggest expense this week?
```

```text
Where am I spending the most money?
```

```text
Analyze my recent spending.
```

## Phase 4 — Personal Finance Assistant

The long-term goal is to turn PocketSpend into a private personal finance assistant.

```text
             PocketSpend
                  │
        ┌─────────┼─────────┐
        │         │         │
     Expenses  Receipts  Budgets
        │         │         │
        └─────────┼─────────┘
                  │
                  ▼
              Local AI
                  │
                  ▼
          Personal Insights
```

---

# 🌟 Vision

Most expense trackers answer:

> "How much did I spend?"

PocketSpend aims to answer:

> "What does my spending mean?"

The goal is to combine expense tracking with private, on-device intelligence.

---

# 🤝 Contributing

Contributions and ideas are welcome.

Create a feature branch:

```bash
git checkout -b feature/my-feature
```

Commit your changes:

```bash
git add .
git commit -m "Add my feature"
```

Push the branch:

```bash
git push origin feature/my-feature
```

Then open a Pull Request.

---

# 📄 License

Add your preferred license here.

Example:

```text
MIT License
```

---

# 👨‍💻 Author

## Chhabinath Sahoo

AI/ML Engineer | Backend Developer

Interested in:

- Generative AI
- LLMs
- On-device AI
- RAG
- AI Agents
- Backend Engineering
- Java
- Python
- Android
- Privacy-focused AI

---

# ⭐ PocketSpend

### Private expenses. Local intelligence. Your data stays yours.
