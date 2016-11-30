# Billboard
Transition the views as billboard.

![](http://7xprgn.com1.z0.glb.clouddn.com/bill.gif)

[See it on Youtube](https://youtu.be/1wO3laIowTw)

# How to include

```gradle
dependencies {
    compile 'com.zql.android:billboard:1.1'
}
```

# Usage

## 1. wirte the code in layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.zql.android.bambooslipdemo.MainActivity">

    <com.zql.android.bambooslip.Billboard
        android:id="@+id/billboard"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:billboard_columns="40"
        app:billboard_rows="1"
        app:billboard_delay="120"
        app:billboard_duration="1200"
        app:billboard_refresh="500"
        app:billboard_orientation="horizontal"
        android:background="@android:color/black"/>

</RelativeLayout>
```

## 2. start Billboard

```java
billboard = (Billboard) findViewById(R.id.billboard);
billboard.setCallback(new Billboard.BillboardCallback() {
    @Override
    public Bitmap getBitmap(int count) {
        if(count%5 == 0){
            return BitmapFactory.decodeResource(getResources(),R.mipmap.b1);
        }
        if(count%5 == 1){
            return BitmapFactory.decodeResource(getResources(),R.mipmap.b2);
        }
        if(count%5== 2){
            return BitmapFactory.decodeResource(getResources(),R.mipmap.b3);
        }
        if(count%5 == 3){
            return BitmapFactory.decodeResource(getResources(),R.mipmap.b4);
        }
        if(count%5 == 4){
            return BitmapFactory.decodeResource(getResources(),R.mipmap.b5);
        }
        return null;
    }
});
billboard.go();
```

# api

## xml
| property  | format | default value |meaning |
| ------- | -------- | :-----------: |-------  |
| billboard_columns  | integer | 10 |  count that billborad will be cut in horizontal    |
| billboard_rows  | integer | 1 |  count that billborad will be cut in vertical    |
| billboard_duration  | integer | 1000 |  animation duration of one slip   |
| billboard_delay  | integer | 300 |  the time delay of next slip do it animatin   |
| billboard_refresh  | integer | 3000 |  time of two image transition   |
| billboard_orientation  | string | horizontal |  orientation of slip's animatin   |

## java

```java
    /**
     * set the callback
     * @param callback {@link BillboardCallback}
     */
    Billboard.setCallback(BillboardCallback callback)

    /**
     * {@link Billboard}'s callback
     */
    public interface BillboardCallback {
        /**
         * get a bitmap to show
         * @param count {0,1,2,3,4,5,6,7,8, ... ,Integer.MAX_VALUE}
         * @return
         */
        Bitmap getBitmap(int count);
    }

    /**
     * start flip
     */
    Billboard.go()

    /**
     * end flip
     */
    Billboard.endFlip()

    /**
     * set timeInterpolator of flip animation
     * @param timeInterpolator
     */
    Billboard.setTimeInterpolator(TimeInterpolator timeInterpolator)
```
# version 1.1
Add a method in BillboardCallback to custom the flip order.
```java
/**
         * custom the delay time of every slip
         * @param index slip index
         * @param delay the time you define in layout xml
         * @param slipSize the size of slip
         * @return delayfactor
         */
        long getDelayFactor(int index,int slipSize,long delay);
```

for example:

**index * delay** make the order from left to right.
```java
@Override
        public long getDelayFactor(int index, int slipSize,long delay) {
            return index * delay;
        }
```
# License

		 Copyright 2016 zhangqinglian

		Licensed under the Apache License, Version 2.0 (the "License");
		you may not use this file except in compliance with the License.
		You may obtain a copy of the License at

 		    http://www.apache.org/licenses/LICENSE-2.0

		Unless required by applicable law or agreed to in writing, software
 		distributed under the License is distributed on an "AS IS" BASIS,
 		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 		See the License for the specific language governing permissions and
 		limitations under the License.
