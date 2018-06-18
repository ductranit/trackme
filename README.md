# trackme
A work out tracking android application

# Screenshots

![Screenshot1](https://raw.githubusercontent.com/ductranit/trackme/master/Screenshots/screenshot1.png)
![Screenshot3](https://raw.githubusercontent.com/ductranit/trackme/master/Screenshots/screenshot3.png)

# Features
- Recording work out session: route, speed, distance & time
- Store unlimited sessions
- Can run on background

# Architecture
Follow [Android Architecture Components sample](https://github.com/googlesamples/android-architecture-components)


# Project structure
- `db` [ObjectBox](http://objectbox.io) database helper classes
- `di` Dependencies injection from [Dagger2](https://github.com/google/dagger)
- `models` app models & db model
- `services` location service & helper classes
- `ui` views & viewmodels, follow [MVVM architecture](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) 
- `utils` utilities & helper classes
- `buildSrc/Dependencies.kt` all versions of dependencies and app configurations
- `src/test` unit tests

License
-------

Copyright 2018 ductranit.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
