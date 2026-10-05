cmake_minimum_required(VERSION 3.22.1)

project("pocketllm")

add_library(pocketllm SHARED
        pocketllm.cpp
)

# llama.cpp headers
target_include_directories(pocketllm PRIVATE
        ${CMAKE_CURRENT_LIST_DIR}/include
        ${CMAKE_CURRENT_LIST_DIR}/ggml/include
)

# llama
add_library(llama SHARED IMPORTED)

set_target_properties(llama PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libllama.so"
)

# ggml
add_library(ggml SHARED IMPORTED)

set_target_properties(ggml PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libggml.so"
)

# ggml-cpu
add_library(ggml-cpu SHARED IMPORTED)

set_target_properties(ggml-cpu PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libggml-cpu.so"
)

# ggml-base
add_library(ggml-base SHARED IMPORTED)

set_target_properties(ggml-base PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libggml-base.so"
)

target_link_libraries(
        pocketllm

        llama
        ggml
        ggml-cpu
        ggml-base

        log
)