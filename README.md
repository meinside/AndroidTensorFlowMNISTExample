# Android TensorFlow MNIST Machine Learning Example

Forked from [MindorksOpenSource/AndroidTensorFlowMNISTExample](https://github.com/MindorksOpenSource/AndroidTensorFlowMNISTExample).

## Changed things are:

Some deprecated or outdated function calls were changed.

Some things were removed or simplified.

---

## How to build library files

Current `libtensorflow_inference.so` and `libandroid_tensorflow_inference_java.jar` were built with TensorFlow [v1.0.1](https://github.com/tensorflow/tensorflow/commit/e895d5ca395c2362df4f5c8f08b68501b41f8a98).

If you want to build them with the newer version of TensorFlow,
   
read [this guide](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/contrib/android/README.md) or do the following:

### Build `libtensorflow_inference.so`

Clone the TensorFlow's repository and edit WORKSPACE file:

```bash
$ cd tensorflow
$ vi WORKSPACE
```

Edit `android_sdk_repository` and `android_ndk_repository` section like this:

```
# Uncomment and update the paths in these entries to build the Android demo.
android_sdk_repository(
    name = "androidsdk",
    api_level = 23,
    build_tools_version = "25.0.1",
    # Replace with path to Android SDK on your system
    path = "/Users/meinside/Documents/files/dev/android-sdk-macosx",
)

# Android NDK r12b is recommended (higher may cause issues with Bazel)
android_ndk_repository(
    name="androidndk",
    path="/Users/meinside/Downloads/android-ndk-r12b",
    api_level=14) # This needs to be 14 or higher to compile TensorFlow.
```

Then run:

```bash
$ bazel build -c opt //tensorflow/contrib/android:libtensorflow_inference.so \
   --crosstool_top=//external:android/crosstool \
   --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
   --cpu=armeabi-v7a
```

If you need `.so` files for other platforms, replace `--cpu=armeabi-v7a` with the desired one:

* `--cpu=arm64`
* `--cpu=x86`
* `--cpu=x86_64`
* ...

Compiled file will be placed at `bazel-bin/tensorflow/contrib/android/libtensorflow_inference.so`.

Copy it into `app/src/main/jniLibs/{PLATFORM_NAME}/`:

```bash
$ mkdir -p {APP_SRC_DIR}/app/src/main/jniLibs/arm64/
$ cp bazel-bin/tensorflow/contrib/android/libtensorflow_inference.so {APP_SRC_DIR}/app/src/main/jniLibs/arm64/
```

#### Note: NDK version

At the time of writing, (2017-03-30)

both TensorFlow 1.0.0 and 1.0.1 fails to build `libtensorflow_inference.so` with latest NDK version(r13).

But I could build it with r12b.

### Build `libandroid_tensorflow_inference_java.jar`

Run:

```bash
$ bazel build //tensorflow/contrib/android:android_tensorflow_inference_java
```

then copy `bazel-bin/tensorflow/contrib/android/libandroid_tensorflow_inference_java.jar` into `app/libs/`:

```bash
$ cp bazel-bin/tensorflow/contrib/android/libandroid_tensorflow_inference_java.jar {APP_SRC_DIR}/app/libs/
```

---

## How to train it yourself

Run `mnist.py` and overwrite generated `mnist_model_graph.pb` into `app/src/main/assets/`.

```bash
$ cd {APP_SRC_DIR}
$ python mnist.py
$ cp model/mnist_model_graph.pb app/src/main/assets/
```

