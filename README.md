# ScrollingLayout

垂直方向支持多个子视图持续连贯滚动的容器，并且支持吸顶功能。

- 支持多个子视图(ScrollView/NestedScrollView仅支持一个子视图)
- 支持连贯滚动，ScrollingView/ScrollView/WebView 可以依次消费滚动距离
- 支持嵌套滚动，实现了 NestedScrollingParent3, NestedScrollingChild3
- 支持吸顶，普通吸顶和常驻吸顶
- 子视图为 RecyclerView 时，不会导致其缓存机制失效
- 子视图为 ViewPager2 时，自动从当前页中获取 ScrollingView



## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:scrollinglayout:0.8.0"
}
```

布局属性

```xml 
<declare-styleable name="ScrollingLayout_Layout">
    <!-- 对齐 -->
    <attr name="layout_gravity" format="enum">
        <enum name="left" value="1" />
        <enum name="right" value="2" />
        <enum name="center" value="3" />
    </attr>
    <!-- 吸顶模式 -->
    <attr name="layout_sticky" format="enum">
        <enum name="none" value="0" />
        <enum name="sticky" value="1" />
        <enum name="permanent" value="2" />
    </attr>
    <!-- 是否允许消费滚动距离，默认为 true -->
    <attr name="layout_allowScrolling" format="boolean" />
    <!-- 是否允许拦截触摸事件，默认为 true -->
    <attr name="layout_allowIntercept" format="boolean" />
    <!-- 是否允许接受嵌套滚动，默认为 true -->
    <attr name="layout_allowNestedScrolling" format="boolean" />
    <!-- 指定消费滚动距离的ScrollingView的视图 -->
    <attr name="layout_scrollingViewId" format="reference">
        <!-- 先找子视图，然后判断自己 -->
        <enum name="auto" value="0" />
        <!-- 仅判断自己 -->
        <enum name="self" value="-1" />
    </attr>
    <attr name="layout_scrollingViewResolver" format="string" />
</declare-styleable>
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).