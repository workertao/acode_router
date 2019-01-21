### 这是一个安卓组件化的方案和组件通信框架 ###

### 感谢 ###
阿里Arouter：[https://github.com/alibaba/ARouter](https://github.com/alibaba/ARouter "Arouter")

apt：[https://www.jianshu.com/p/13b9adb17010](https://www.jianshu.com/p/13b9adb17010 "apt")

### Arouter分析 ###

我们在工作中总会遇到这种情况，随着项目需求的不断增加，业务逻辑越来越复杂，而在不同的业务之间难免会出现耦合的模块。或者在修改一些小功能的时候，总是要不情愿的去花费大量的时间编译整个项目，最后只为了测试那个小小的功能。而组件化正是为了解决这两个恶心的问题，我们将业务组件化之后，各个业务层之间互不关联，大大的降低了耦合。各个业务层可以编译打包成单独的apk运行，方便调试。而这些业务之间的通信我们使用路由来打通。

这两天在研究阿里的Arouter，画了一张图记录下。

![阿里路由](https://raw.githubusercontent.com/workertao/acode_router/master/img/arouter.png)

首先我们给业务组件的Activity增加注解，在编译的过程中，通过APT(Annotation Processing Tool)是一种处理注释的工具，它对源代码文件进行检测找出其中的注解，使用注解进行额外的处理。然后使用javapoet生成相应的代码。最后会在build文件中生成两个相应的类文件(此处说两个是参照Arouter，数量无上限，完全自定义)。

第一个文件：用来存放当前moudle下所有被注解过的类的map集合。（分组Main$$Group）

第二个文件：用来存放第一个文件的map集合。（存放分组Main$$Root）

其实阿里的路由最主要的就是这两个文件的来源以及它的作用，把他俩搞清楚，整个路由的框架就很清晰了。我感觉可以总结成一句话，就是将注解的Activity信息(对应key)存放在GroupMap中，然后将GroupMap(对应GroupKey)存放在RootMap中。然后通过GroupKey在RootMap中查找到GroupMap，然后在通过key在GroupMap查找到对应的路由信息。

整个注解过程是在编译的时候写入到我们的项目中，并且生成了相的.java文件。这样我们在运行的时候，通过路由找到map下边对应的路由信息，就可以进行跳转了。

整体的思路大概就是这样，阿里的Arouter还对Activity参数进行了包装，我这里未对Activity传参进行包装，所有的数据都通过Bundle传递。阿里的Arouter还可以Fragment之间通信，我这块未对Fragment进行处理，因项目周期比较紧张，而且人手有限，不打算进行更细层次的业务划分，业务太多对于人少的公司来说维护也是成本，所以目前只做了Activity的通信，足够完成现在的需求。


### 使用方式 ###

- 初始化(最好在Application)

		BjxSuperRouter.getInstance().init(this);

- 在需要路由的module的gradle中添加

	    annotationProcessor project(':bjxRouterProcessor')
	    implementation project(':bjxRouterCommon')

- 添加注解
		
		@BjxRouter(path = "/bjxCommunity/BjxCommunityActivity")
		public class BjxCommunityActivity extends Activity {
			 @Override
    		protected void onCreate(@Nullable Bundle savedInstanceState) {
        		super.onCreate(savedInstanceState);
    		}
		}

- startActivity

		BjxSuperRouter.getInstance().build("/bjxNews/BjxNewActivity").navigation(BjxCommunityActivity.this);

- startActivityForResult
		
		  BjxSuperRouter.getInstance().build("/bjxNews/BjxNewActivity").withBundle(bundle).navigation(BjxCommunityActivity.this, 203);

- 传参和跳转监听

			    Bundle bundle = new Bundle();
                bundle.putString("name", "古力娜扎-巴扎黑");
                bundle.putInt("age", 458853);
                BjxSuperRouter.getInstance().build("/bjxNews/BjxNewActivity").withBundle(bundle).navigation(BjxCommunityActivity.this, 203, new NavigationCallback() {
                    @Override
                    public void onFound(PostCard postcard) {
                        Log.d("router","onFound:"+postcard.toString());
                    }

                    @Override
                    public void onLost(PostCard postcard) {
                        Log.d("router","onLost:"+postcard.toString());
                    }

                    @Override
                    public void onArrival(PostCard postcard) {
                        Log.d("router","onArrival:"+postcard.toString());
                    }
                });

### 将module单独打包apk运行 ###

一个module是一个lib还是一个Application是通过gradle中的配置来确定的，我们以这个为切入点就可以实现将moudle进行lib和Application的自由切换了。

- 项目根gradle
 
	 	ext {
	        //组件化配置  true（组件化，每个module可以单独运行） false(app是主项目，其他module全是lib)
	        isModule = true
	    }

- moudle gradle

	
		if (isModule) {
		    apply plugin: 'com.android.application'
		} else {
		    apply plugin: 'com.android.library'
		}

- module资源文件配置


        if (isModule) {
            //设置appid
            applicationId appid.bjxCommumity
        }
        //资源配置
        sourceSets {
            main {
                //在组件模式下 使用不同的manifest文件
                if (isModule) {
                    manifest.srcFile 'src/main/runalone/AndroidManifest.xml'
                    java.srcDirs 'src/main/runalone'
                } else {
                    manifest.srcFile 'src/main/AndroidManifest.xml'
                    java.srcDirs 'src/main/java'
                }
            }
        }
