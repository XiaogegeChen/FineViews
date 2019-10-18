[![](https://jitpack.io/v/XiaogegeChen/FineViews.svg)](https://jitpack.io/#XiaogegeChen/FineViews)
# 1. ColorTextView （渐变色文字）
## 效果图
![0](https://github.com/XiaogegeChen/FineViews/blob/master/screenshot/color_text_view.png)
## 快速使用
1.在工程根目录的build.gradle中添加依赖
```
allprojects {
    repositories {
        google()
        jcenter()
        
        maven { url 'https://jitpack.io' } 
    }
}
```
2.在工程目录的build.gradle中添加依赖(1.0.0版本或更高)
```
implementation 'com.github.xiaogegeChen:fineviews:1.0.0'
```
3.在xml中配置
```
<com.github.xiaogegechen.library.ColorTextView
        android:id="@+id/color_text_view"
        android:layout_width="200dp"
        android:layout_height="200dp"
        app:start_color="@color/color_text_view_start_color"
        app:end_color="@color/color_text_view_end_color"
        app:text="98"
        app:text_size="100sp"/>
```

## 可配置的属性
```app:start_color```   渐变开始颜色<br/>
```app:end_color```   渐变结束颜色<br/>
```app:text```   文字<br/>
```app:text_size```   文字字号<br/>
```app:text_gravity```   文字位置，取值与```android:gravity```相同<br/>
# 2. CornerButton （带图标的圆角button）
## 效果图
![0](https://github.com/XiaogegeChen/FineViews/blob/master/screenshot/corner_button.png)
## 快速使用
1.在工程根目录的build.gradle中添加依赖
```
allprojects {
    repositories {
        google()
        jcenter()
        
        maven { url 'https://jitpack.io' } 
    }
}
```
2.在工程目录的build.gradle中添加依赖(1.2.0版本或更高)
```
implementation 'com.github.xiaogegeChen:fineviews:1.2.0'
```
3.在xml中配置
```
<com.github.xiaogegechen.library.CornerButton
        android:id="@+id/share_button"
        android:layout_marginLeft="20dp"
        android:layout_width="100dp"
        android:layout_height="40dp"
        app:corner_button_icon="@drawable/qq"
        app:corner_button_border_color="#000000"
        app:corner_button_distance="1dp"
        app:corner_button_text_size="20sp"
        app:corner_button_text="分享"/>
```

## 可配置的属性
```app:corner_button_start_color```   渐变开始颜色<br/>
```app:corner_button_end_color```   渐变结束颜色<br/>
```app:corner_button_border_color```   边框颜色<br/>
```app:corner_button_text_color```   文字颜色<br/>
```app:corner_button_border_width```   边框线宽<br/>
```app:corner_button_text```   文字<br/>
```app:corner_button_text_size```   文字字号<br/>
```app:corner_button_icon```   图标<br/>
```app:corner_button_distance```   文字与图标的间距<br/>
## 更新日志
v1.1.0
增加CornerButton

v1.2.0
* ColorTextView添加```text_gravity```属性，取值与```android:gravity```相同
* ColorTextView在宽高指定时，如果文字显示不下，将不会显示
* 优化尺寸未指定时的尺寸确定方式，取消默认大小，自动测量

