本篇README包含以下内容:

第一部分 : 本项目内容介绍
第二部分 : 如何编译
第三部分 : 如何运行交叉检验
第四部分 : 交叉检验输入格式
第五部分 : 交叉检验输出格式
第六部分 : 如何运行ui界面及测试单个文件
第七部分 : ui界面及测试单个文件输入格式
第八部分 : ui界面及测试单个文件输出格式
第九部分 : 相关文献
第十部分 : 附录

========================================================================

第一部分 : 本项目内容介绍

文章关键词提取算法(包含交叉测试)

build :　运行目录

GIZA++  mkcls  plain2snt.out : 执行WTM/WAM/WAM*算法所需文件

========================================================================

第二部分 : 如何编译

编译环境 : java 环境(支持java 1.8.0)

ant编译 : 在TagSuggestion/目录下打开终端,输入命令 ant ,即可完成编译;或可在eclipse下右键build.xml选择"ant build"操作.

========================================================================

第三部分 : 如何运行交叉检验

交叉检验测试单个算法 : 具体命令参考command文件,训练类与测试类一一对应.(训练类与测试类对应关系见附录)

各个参数含义 : --dataset="输入文件路径"
	      --trainer_class="训练类名称"
	      --suggester_class="测试类名称"
	      --num_folds="输入文件分割份数"
	      --config="参数1=值1;参数2=值2;..."(参数可以是dataType,k,numtopics,niter等等,不同模型参数不同,不输入即使用默认值)
	      --working_dir="工作目录"
	      --report="运行结果存放路径"

默认book.model和chinese_stop_word.txt路径和tagsuggest.jar文件位于同一目录下.如果需要更改book.model路径信息,需要在运行命令的config中添加model="book.model路径",其中book.model用于最大前向分词，chinese_stop_word.txt记录中文中的停用词

如果需要运行SMT算法,需要额外的三个文件GIZA++  mkcls  plain2snt.out,默认路径为这三个文件和tagsuggest.jar文件位于同一目录下.如果需要更改路径信息,需要在运行命令参数中添加  --giza_path="这三个文件的路径"

In Doubanpost (Md=2)

| algorithm | p | r | F1 |
|---|:---|:---|:---|
| WTM | 0.36828 | 0.45131 | 0.35410 |
| PMI | 0.31453	| 0.30884 | 0.26602 |
| KNN |	0.27914 | 0.26815 | 0.23365 |
| NaiveBayes |	0.23707 | 0.21926 | 0.19369 |
| TPR | 0.21436 |0.18183 | 0.16829 |
| NoiseTagLdaModel | 0.21206 | 0.17785 | 0.16538 |
| ExpandRankKE | 0.15213 | 0.16313 | 0.13441 |
| WAM | 0.15112 | 0.15756 | 0.13172 |
| TagLdaModel | 0.16191 | 0.13882 | 0.12801 |
| TFIDF | 0.12962 | 0.14525 | 0.11725 |
| WAMsample | 0.10288 | 0.10819 | 0.08945 |

In Keywordpost (Md=2)

| algorithm | p | r | F1 |
|---|:---|:---|:---|
| TFIDF | 0.21508 | 0.24943 | 0.22591 |
| ExpandRankKE | 0.20291 | 0.23672 | 0.21368 |
| NaiveBayes | 0.19729 | 0.24835 | 0.21267 |
| WAMsample | 0.19841 | 0.22931 | 0.20836 |
| WAMwithtitleInstead | 0.19721 | 0.22687 | 0.20676 |
| KNN | 0.19014 | 0.23957 | 0.20463 |
| TAM | 0.10893 | 0.1293 | 0.1153 |
| NoiseTagLdaModel | 0.07388 | 0.09015 | 0.07902 |
| TPR | 0.07315 | 0.08589 | 0.07732 |
| TagLdaModel | 0.04871 | 0.05879 | 0.05194 |


========================================================================

第四部分 : 交叉检验输入格式

dataType=Post : {"id":"文章编号","content":"文章内容","extras":"","resourceKey":"","timestamp":0,"title":"文章标题","userId":"","tags":["标签1","标签2","标签3",...]}   (侧重新闻题材)
	 示例 : {"id":"1004838","content":"格非――1964年生于江苏省丹德县，毕业于华东师范大学中文系，获文学博士学位。1985年至2000年任教于华东师范大学，现为清华大学中文系教授。主要作品有《格非文集》、《欲望的旗帜》等。","extras":"","resourceKey":"","timestamp":0,"title":"敌人","userId":"","tags":["小说","荒诞","敌人","中国文学","格非"]}   (样例文件可参考post.dat/keypost.dat)

dataType=DoubanPost : {"doubanTags":{"标签1":权重1(在此可用豆瓣点赞数表示权重),"标签2":权重2,"标签3":权重3,...},"id":"文章编号","content":"文章内容","tags":[标签],"timestamp":0,"resourceKey":"","title":"文章标题","userId":"","extras":""}   (侧重书籍)
	       示例 : {"doubanTags":{"文化":5,"献给非哲学家的小哲学":6,"哲学":29,"法国":17},"id":"1000047","content":"全球化是必然趋势？仁者见仁，智者见智。有人惊呼：“狼来了！”有人担忧：“怎么办？”还有人在思考：“对世界来说，经济可以全球化，甚至货币也可以一体化，但文化则要鼓励多元化。”是的，只有本着文化多元化的精神，在尊重其他民族文化的同时，自身才能获得不断的发展与丰富。法国人做出了自己的探索与努力。今天，您面前的这一套“法兰西书库·睿哲系列”为您打开了一扇沟通的窗口。他山之石，可以攻玉。我们希望这样的对话可以走得越来越远。","tags":[],"timestamp":0,"resourceKey":"","title":"献给非哲学家的小哲学  睿哲系列","userId":"","extras":""}   (样例文件可参考bookPost70000.dat)

========================================================================

第五部分 : 交叉检验输出格式

输出为文本,前七列为主要数据,从第一列至第七列依次为 
|评测算法输出的关键词的个数 | 准确率(Pre.) | 准确率的方差 | 召回率(Rec.) | 召回率的方差 | Fmeans(F.) | Fmeans的方差 |

其中1 / Fmeans = 1 / Pre. + 1 / Rec. 

========================================================================

第六部分 : 如何运行ui界面及测试单个文件

训练model命令 : java -Xmx8G -jar tagsuggest.jar train.TrainWTM --input=/home/meepo/test/sampleui/bookPost70000.dat --output=/home/meepo/test/sample --config="dataType=DoubanPost;para=0.5;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2"
input为训练数据地址,output为模型输出地址,config为模型参数

打开ui界面命令 : java -Xmx8G -jar tagsuggest.jar evaluation.GuiFrontEnd --model_path=/home/meepo/test/sampleui/  --algorithm=SMTTagSuggest --config="" --realtime=true
model_path为模型地址,algorithm为选择测试类,config为测试类参数

测试单个文件 : java -Xmx8G -jar tagsuggest.jar evaluation.TestDemo --model_path=/home/meepo/test/sampleui/ --algorithm=SMTTagSuggest --config="" --article_path=/home/meepo/text --output_path=/home/meepo/tag

model_path为模型地址,algorithm为选择测试类,config为测试类参数,article_path为需测试文件(第一行为标题,第二行为内容),output_path为关键词文件输出路径

默认book.model和chinese_stop_word.txt路径和tagsuggest.jar文件位于同一目录下.如果需要更改book.model路径信息,需要在运行命令的config中添加model="book.model路径"

如果需要运行WTM/WAM/WAM*算法,需要额外的三个文件GIZA++  mkcls  plain2snt.out,默认路径为这三个文件和tagsuggest.jar文件位于同一目录下.如果需要更改路径信息,需要在运行命令参数中添加  --giza_path="这三个文件的路径"

========================================================================

第七部分 : ui界面及测试单个文件输入格式

ui界面直接输入
测试单个文件的输入文件需包含两行，第一行为文章标题，第二行为文章内容

========================================================================

第八部分 : ui界面及测试单个文件输出格式

ui界面为可视化输出
测试单个文件会输出一个文本文件，里面包含输入文章的十个预测关键词和它们在算法中相应的权重

========================================================================

第九部分 : 相关文献



========================================================================

第十部分 : 附录

训练类与测试类对应关系

TrainExpandRank            --  ExpandRankKE
TrainKnn                   --  KnnTagSuggest
TrainNaiveBayes            --  NaiveBayesTagSuggest
TrainNoiseTagLdaModel      --  NoiseTagLadaTagSuggest
TrainPMI                   --  PMITagsuggest
TrainTagLdaModel           --  TagLdaTagSuggest
TrainTAM                   --  TAMTagSuggest
TrainTFIDF                 --  TFIDFTagSuggest
TrainTPR                   --  TPRTagSuggest
TrainWAM                   --  SMTTagSuggest.java
TrainWAMsample.java        --  SMTTagSuggest.java
TrainWAMWithtitleInstead   --  SMTTagSuggest.java 
TrainWTM.java              --  SMTTagSuggest.java

========================================================================
