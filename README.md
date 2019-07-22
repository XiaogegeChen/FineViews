[![](https://jitpack.io/v/XiaogegeChen/FineViews.svg)](https://jitpack.io/#XiaogegeChen/FineViews)
# ColorTextView （渐变色文字）
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
2.在工程目录的build.gradle中添加依赖(查看最上面的版本号进行替换)
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

## 可配置的属性（请更新至最新版本）
```app:start_color```   渐变开始颜色<br/>
```app:end_color```   渐变结束颜色<br/>
```app:text```   文字<br/>
```app:text_size```   文字字号<br/>
## 更新日志
