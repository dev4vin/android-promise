# Promise App
- [v1.0.0]
Manage your todo items, notes, expenses, See current weather and many more

- Please read also [issues](https://github.com/dev4vin/android-promise/issues) 
### Features
* Todo items
* Keeping notes
* Managing expenses
* Seeing current weather information

### Other _experimental_ features
* Geo location, geo fencing, activity recognition.
* Websockets and data streaming

# Setup
- 
#### build.gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}


dependencies {
     implementation 'com.github.dev4vin:android-promise:1.2.6-beta07'
}
```

# Initialization
Initialize Promise in your main application file, entry point

#### App.java
```java
  @Override
  public void onCreate() {
    super.onCreate();
    Promise.init(this).threads(100);
  }
```
#### Stay Updated

# Wiki!
I strongly recommend to read the **[Wiki](https://github.com/dev4vin/android-promise/wiki) pages**, where you can find a comprehensive Tutorial.<br/>

### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>

# License

#### Android Promise

    Copyright 2018 Peter Vincent

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

