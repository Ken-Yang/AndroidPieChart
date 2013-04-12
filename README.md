# AndroidPieChart

**AndroidPieChart is a tool that you can generate a fancy pie chart easily on your android app.**



![AndroidPieChart](https://dl.dropboxusercontent.com/u/13846584/screen.png "AndroidPieChart")  



## Requirements
AndroidPieChart needs to depend on the following environment:
    
    1. Java 1.6 or above
    2. Eclipse
    3. Android SDK

## Deployment

### 1. Get repository
Please clone this lib to your workspace first.

    git clone https://github.com/Ken-Yang/AndroidPieChart.git
    
### 2. Setup
#### 2.1 Import to Eclipse
    
    File->Import->Existing Projects into Workspace
    
#### 2.2 Reference this lib to your project
    
    Right Click on your project -> Properties -> Android -> Add -> <<Select AndroidPieChart>> -> OK

![AndroidPieChart](http://dl.dropbox.com/u/13846584/Screen%20Shot%202013-04-03%20at%206.12.54%20PM.png "AndroidPieChart")  


## How to use?

### 1. Add PieChart view element in your layout.xml
```XML
<net.kenyang.piechart.PieChart
  android:id="@+id/pieChart"
  android:layout_width="match_parent"
  android:layout_height="match_parent">
</net.kenyang.piechart.PieChart>
```

### 2. Get the Object for drawing a Pie Chart
```Java
final PieChart pie = (PieChart) findViewById(R.id.pieChart);
```

### 3. Prepare the data
Please ensure your total data percentage is equal to 100%, otherwise the lib will throw Exception and show nothing.
```Java
ArrayList<Float> alPercentage = new ArrayList<Float>();
alPercentage.add(10.0f);
alPercentage.add(20.0f);
alPercentage.add(10.0f);
alPercentage.add(10.0f);
alPercentage.add(10.0f);
alPercentage.add(10.0f);
alPercentage.add(10.0f);
alPercentage.add(10.85f);
alPercentage.add(9.15f);
```

### 4. Draw pie chart with the data
```Java
try {
  // setting data
  pie.setAdapter(alPercentage);
  
  // setting a listener 
  pie.setOnSelectedListener(new OnSelectedLisenter() {
    @Override
    public void onSelected(int iSelectedIndex) {
      Toast.makeText(ChartView.this, "Select index:" + iSelectedIndex, Toast.LENGTH_SHORT).show();
    }
  });  
} catch (Exception e) {
  if (e.getMessage().equals(PieChart.ERROR_NOT_EQUAL_TO_100)){
    Log.e("kenyang","percentage is not equal to 100");
  }
}
```

## License
Copyright 2013 Ken Yang
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.



