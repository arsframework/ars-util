# ars-util
Arsframework util模块提供了针对日常处理需要的工具方法，如日期、文档、Excel、字符串等。

## 1 环境依赖
JDK1.8+

## 2 部署配置
在Maven配置中添加如下依赖：
```
<dependency>
    <groupId>com.arsframework</groupId>
    <artifactId>ars-util</artifactId>
    <version>1.4.7</version>
</dependency>
```

## 3 功能描述

### 3.1 com.arsframework.util.Dates
该抽象类提供了针对日期处理的工具方法，如日期转换、计算等。

### 3.2 com.arsframework.util.Documents
该抽象类提供了针对文档处理的工具方法，如SVG转PDF、HTML转PDF等。

### 3.3 com.arsframework.util.Excels
该抽象类提供了针对Excel处理的工具方法，如Excel读写。

### 3.4 com.arsframework.util.Files
该抽象类提供了针对文件处理的工具方法，如文件创建、删除、移动、读写、查找塞选等。

### 3.5 com.arsframework.util.Objects
该抽象类提供了针对对象处理的工具方法，如对象反射操作、对象比较、对象转换等。

### 3.6 com.arsframework.util.Randoms
该抽象类提供了针对随机数处理的工具方法，如随机生成字符串、数字、日期、对象等。

### 3.7 com.arsframework.util.Secrets
该抽象类提供了针对数据加解密处理的工具方法，如MD5加密、DES加解密、AES加解密等。

### 3.8 com.arsframework.util.Streams
该抽象类提供了针对数据流处理的工具方法，如对象序列化与反序列化、数据流读写等。

### 3.9 com.arsframework.util.Strings
该抽象类提供了针对字符串处理的工具方法，如进制转换、匹配、合并、条件转换等。

### 3.10 com.arsframework.util.Webs
该抽象类提供了针对Web应用处理的工具方法，如```Cookie```操作、视图渲染等。

### 3.11 com.arsframework.util.Asserts
该抽象类提供了断言处理的工具方法，如非Null、非空、大小、长度验证等。

### 3.12 com.arsframework.util.Barcodes
该抽象类提供了针对条形/二维码操作的工具方法，如对条形/二维码的数据编码与解码等。

### 3.13 com.arsframework.util.Opcodes
该抽象类提供了针对验证码操作的工具方法，如将验证码编码成图片等。

## 4 版本更新日志
### v1.0.4
1. 内部优化
2. 更新```ars-annotation```依赖版本号为```1.3.2```

### v1.0.5
1. 新增```com.arsframework.util.Asserts```断言处理工具类
2. 优化```com.arsframework.util.Excels```处理逻辑
3. 更新```ars-annotation```依赖版本号为```1.4.0```

### v1.1.0
1. 内部优化及Bug修复
2. 移除已过时的类和方法

### v1.2.0
1. 内部优化及Bug修复
2. 移除已过时的类和方法

### v1.3.0
1. 内部优化及Bug修复
2. 移除```com.arsframework.util.Jsons```工具类

### v1.3.1
1. 新增针对```Excel2007```版大文件数据遍历方法

### v1.4.0
1. 内部优化
2. 更新```xerces```包依赖
3. 更新```ars-annotation```包依赖

### v1.4.1
1. 内部优化
2. 更新```ars-annotation```包依赖

### v1.4.2
1. 新增```com.arsframework.util.Barcodes```、```com.arsframework.util.Opcodes```工具类
2. 优化```com.arsframework.util.Excels```、```com.arsframework.util.Files```、```com.arsframework.util.Dates```工具类

### v1.4.3
1. 优化```com.arsframework.util.Objects```、```com.arsframework.util.Randoms```工具类

### v1.4.4
1. 更新```ars-annotation```包依赖
2. 优化```com.arsframework.util.Asserts```工具类

### v1.4.5
1. 优化```com.arsframework.util.Strings```工具类

### v1.4.6
1. 优化```com.arsframework.util.Objects```、```com.arsframework.util.Strings```、```com.arsframework.util.Webs```工具类

### v1.4.7
1. 移除```com.arsframework.util.Objects.Adapter```接口
2. 完善及优化```com.arsframework.util.Excels```工具类功能