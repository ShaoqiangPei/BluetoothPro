# BluetoothPro
a library for bluetooth

## 简介
BluetoothPro 是一个专注蓝牙连接的工具库，目的是使涉及到蓝牙相关功能开发变得更加高效快速。

## 依赖
在你 project 对应的 build.gradle 中添加以下代码：
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
在你需要引用的 module对应的build.gradle中(此处以app_module的build.gradle中引用 0.0.1版本为例)添加版本依赖：
```
	dependencies {
        implementation 'com.github.ShaoqiangPei:BluetoothPro:0.0.1'
	}
```

## 使用说明
