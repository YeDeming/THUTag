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

第十部分 : 作者

第十一部分 : 附录


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

交叉检验测试单个算法 : 具体命令参考command文件,训练类与建议类一一对应.(训练类与建议类对应关系见附录)

各个参数含义 : --dataset="输入文件路径"
	      --trainer_class="训练类名称"
	      --suggester_class="建议类名称"
	      --num_folds="输入文件分割份数"
	      --config="参数1=值1;参数2=值2;..."(参数可以是dataType,k,numtopics,niter等等,不同模型参数不同,不输入即使用默认值)
	      --working_dir="工作目录"
	      --report="运行结果存放路径"

默认book.model和chinese_stop_word.txt路径和tagsuggest.jar文件位于同一目录下.如果需要更改book.model路径信息,需要在运行命令的config中添加model="他们共同的路径",其中book.model用于最大前向分词，chinese_stop_word.txt记录中文中的停用词。 e.g. --config="model=/home/meepo/TagSuggestion;dataType=DoubanPost;"

如果需要运行SMT算法,需要额外的三个文件GIZA++  mkcls  plain2snt.out,默认路径为这三个文件和tagsuggest.jar文件位于同一目录下.如果需要更改路径信息,需要在运行命令参数中添加  --giza_path="这三个文件的路径"

The evaluation results on Douban Post Dataset (M_d=3)

| Algorithm | Precision | Recall | F1 |
|---|:---|:---|:---|
| PMI | 0.38962	| 0.45730 | 0.36692 |
| WTM | 0.36828 | 0.45131 | 0.35410 |
| KNN |	0.35862 | 0.40057 | 0.32871 |
| TAM | 0.31710 | 0.35261 | 0.29026 |
| NaiveBayes |	0.25466 | 0.28118 | 0.23079 |
| NoiseTagLdaModel | 0.22596 | 0.22855 | 0.19665 |
| TagLdaModel | 0.17459 | 0.18503 | 0.15623 |
| TFIDF | 0.12962 | 0.14525 | 0.11725 |


The evaluation results on Keyword Post Dataset (M_d=2)

| Algorithm | Precision | Recall | F1 |
|---|:---|:---|:---|
| WAM | 0.30735 | 0.43726 | 0.34747 |
| WAMsample | 0.29424 | 0.41814 | 0.33254 |
| WAMwithtitleInstead | 0.26571 | 0.37286 | 0.29849 |
| ExpandRankKE | 0.22818 | 0.31578 | 0.25461 |
| TPR | 0.21913 | 0.3060 | 0.24551 |
| TFIDF | 0.25459 | 0.20083 | 0.21876 |
| Textpagerank | 0.19833 | 0.22971 | 0.20837 |


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

其中2 / Fmeans = 1 / Pre. + 1 / Rec. 

========================================================================

第六部分 : 如何运行ui界面及测试单个文件

训练model命令 : java -Xmx8G -jar tagsuggest.jar train.TrainWTM --input=/home/meepo/test/sampleui/bookPost70000.dat --output=/home/meepo/test/sample --config="dataType=DoubanPost;para=0.5;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2"
input为训练数据地址,output为模型输出地址,config为模型参数

打开ui界面命令 : java -Xmx8G -jar tagsuggest.jar evaluation.GuiFrontEnd --model_path=/home/meepo/test/sampleui/  --algorithm=SMTTagSuggest --config="" --realtime=true
model_path为模型地址,algorithm为选择测试类,config为测试类参数

测试单个文件 : java -Xmx8G -jar tagsuggest.jar evaluation.TestDemo --model_path=/home/meepo/test/sampleui/ --algorithm=SMTTagSuggest --config="" --article_path=/home/meepo/text --output_path=/home/meepo/tag

model_path为模型地址,algorithm为选择测试类,config为测试类参数,article_path为需测试文件(第一行为标题,第二行为内容),output_path为关键词文件输出路径

默认book.model和chinese_stop_word.txt路径和tagsuggest.jar文件位于同一目录下.如果需要更改book.model路径信息,需要在运行命令的config中添加model="他们共同的路径",其中book.model用于最大前向分词，chinese_stop_word.txt记录中文中的停用词。 e.g. --config="model=/home/meepo/TagSuggestion;dataType=DoubanPost;"

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

第十部分 : 作者
刘知远、陈新雄、叶德铭


========================================================================

第十一部分 : 附录

训练类与建议类对应关系

| Training Class | Suggesting Class |
|---|:---|
| TrainExpandRank | ExpandRankKE |
| TrainKnn | KnnTagSuggest |
| TrainNaiveBayes | NaiveBayesTagSuggest |
| TrainNoiseTagLdaModel | NoiseTagLadaTagSuggest |
| TrainPMI | PMITagsuggest |
| TrainTagLdaModel | TagLdaTagSuggest |
| TrainTAM | TAMTagSuggest |
| TrainTFIDF | TFIDFTagSuggest |
| TrainTPR | TPRTagSuggest |
| TrainWAM | SMTTagSuggest |
| TrainWAMsample | SMTTagSuggest |
| TrainWAMWithtitleInstead | SMTTagSuggest | 
| TrainWTM | SMTTagSuggest |
| TrainTextpagerank | TextpagerankTagSuggest |

========================================================================

