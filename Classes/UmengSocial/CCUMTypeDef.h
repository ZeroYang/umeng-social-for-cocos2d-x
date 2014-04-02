//
//  CCUMTypeDef.h
//  Singleton
//
//  Created by mrsimple on 3/13/14.
//  Copyright (c) 2014 mrsimple. All rights reserved.
//

#include <map>
#include <string>
using namespace std;

#ifndef _CCUMTypeDef_h
#define _CCUMTypeDef_h

//#include "cocos2d.h"

//USING_NS_CC;

#ifdef __cplusplus
    #define USING_NS_UM_SOCIAL      using namespace umeng::social
#else 
    #define USING_NS_SOCIAL 
#endif 

namespace umeng
{
    namespace social 
    {
        enum Platform{
        SINA = 0,               // 新浪微博
        WEIXIN = 1,             // 微信
        WEIXIN_CIRCLE = 2,      // 微信朋友圈
        QQ = 3,                 // QQ
        QZONE = 4,              // QQ空间
        RENREN = 5,             // 人人网
        DOUBAN = 6,             // 豆瓣
        LAIWANG = 7,            // 来往
        LAIWANG_CIRCLE = 8,     // 来往动态
        YIXIN = 9,              // 易信
        YIXIN_CIRCLE = 10,      // 易信朋友圈
        FACEBOOK = 11,          // facebook
        TWITTER = 12,           // twitter
        INSTAGRAM = 13,         // instagram
        SMS = 14,               // 短信
        EMAIL = 15,             // 邮件
        TENCENT_WEIBO = 16      // 腾讯微博
    };

    // // 网络请求回调状态
    // enum State { 
    //     ST_START = 0,                  // 分享启动
    //     ST_ERROR = 1,                  // 错误
    //     ST_CANCEL = 2,                 // 取消
    //     ST_COMPLETE = 3                // 分享完成

    //  };

    // 最好还是添加一个字符串数组参数用来传递字符串信息, char* args[];
    typedef void (*AuthEventHandler) (int platform, int stCode,const map<string, string>& data);
    typedef void (*ShareEventHandler) (int platform,int stCode,const string& errorMsg);

    // 
    #define auth_selector(_SELECTOR) (AuthEventHandler)(&_SELECTOR)
    #define share_selector(_SELECTOR) (ShareEventHandler)(&_SELECTOR)

    }
}

#endif
