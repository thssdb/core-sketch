Benchmark for Verification and Double-Write
---

# 1. 正确性验证

## 1.1. environment
目前仅仅支持IoTDB v0.12和TimescaleDB进行正确性验证

## 1.2. 相关模式
1. testWithDefaultPath 使用benchmark生成数据集后正确性验证。
2. generateDataMode 生成相关数据集。
3. verificationWriteMode 将本地生成的数据集双写进入不同数据库。
4. verificationQueryMode 根据本地生成的数据集进行点对点查询，确认数据库中数据点正确。

## 1.3. 正确性验证方法

### 1.3.1. 不记录数据集直接验证
> 相关Benchmark运行模式：testWithDefaultPath

1. 首先使用Benchmark对两个数据库进行写入，相关配置文件为[write config](conf/write.properties)
2. 然后使用Benchmark对两个数据库进行查询比较，相关配置文件为[query config](conf/query.properties)

### 1.3.2. 记录数据集后进行验证
> 相关Benchmark运行模式：generateDataMode、verificationWriteMode、verificationQueryMode

1. 首先使用generateDataMode生成本地数据集，相关配置文件为[generate config](conf/generate.properties)
2. 然后使用verificationWriteMode向两个数据库写入数据集，相关配置文件为[generate write config](conf/generate-write.properties)
3. 最后使用verificationQueryMode查询两个数据库中数据集（点查询），相关配置文件为[generate query config](conf/generate-query.properties)

# 2. 双写模式
1. 双写模式仅支持不同数据库之间进行比较，不支持同一个数据库不同版本进行双写
2. 对于influxdb只支持v2.0+