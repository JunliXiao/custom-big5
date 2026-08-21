# my-big5

## JCConf 2026

針對臺灣大型舊系統中 Big5 擴充難字導致的技術債難題，傳統作法經常伴隨多用戶衝突、繁瑣的自造輪子、暴力查表等痛點。本短講將分享動態字元集重構方案，透過翻轉 Java 底層為「動態實例化」架構，實現多用戶隔離並動態讀取外部對映表。此機制結合標準 API 調用與混血擴充區塊，在兼顧效能的同時，還將複雜性藏在 Charset 和 String 的底下。

[簡報連結](https://docs.google.com/presentation/d/1Rb8FNkSa5JhJPZiYMyl3gKGWMq24Fed8R7WNiwwFWAw/edit?usp=sharing)

## Build & Use
JDK 8
- 將 mapping_data 放到適當路徑，依此修改 Config
- `mvn package`
- 在所需專案裡引進 my-big5.jar

> 這份 repo 為示範、參考性質。

## Credits

Regulus Technologies 銳力科技股份有限公司

## License

[GNU General Public License v2.0 with Classpath Exception](./LICENSE).