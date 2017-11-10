package com.ijpay.controller.weixin;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.jpay.ext.kit.IpKit;
import com.jpay.ext.kit.PaymentKit;
import com.jpay.ext.kit.StrKit;
import com.jpay.weixin.api.WxPayApiConfig;
import com.jpay.weixin.api.WxPayApiConfigKit;
import com.jpay.weixin.api.hb.ReadHbModle;
import com.jpay.weixin.api.hb.RedHbApi;

/**
 * 微信现金红包
 * @author dujianyang
 *
 */
public class WxRedPackController extends WxPayApiController {
	
	static Log log=Log.getLog(WxRedPackController.class);
	private static final Prop prop = PropKit.use("wxpay.properties");
	 //商户相关资料
	//微信appid
    String appid = prop.get("appId");
    //微信支付分配的商户号
    String mch_id = prop.get("mchId");
    //证书路径
    String certPath =  prop.get("certPath");//D:\\cert\\apiclient_cert.p12
    //API密钥 商户签名key
    String partnerKey = prop.get("partnerKey");

    @Override
    public WxPayApiConfig getApiConfig() {
        WxPayApiConfig apiConfig = WxPayApiConfig.New()
                .setAppId(appid)
                .setMchId(mch_id)
                .setPaternerKey(partnerKey)
                .setPayModel(WxPayApiConfig.PayModel.BUSINESSMODEL);
        return apiConfig;
    }
	
	 /**
     * 微信普通红包
     *
     * @param wechatRedPack
     * @param request
     * @return
     */
    public void sendRedPack() {
            WxPayApiConfig config = getApiConfig();
            WxPayApiConfigKit.setThreadLocalWxPayApiConfig(config);
            String ns = String.valueOf(System.currentTimeMillis());
            Map<String, String> params = ReadHbModle.Builder()
            		.setPayModel(config.getPayModel())
                    .setHbType(ReadHbModle.HbType.NORMAL)//普通红包
                    .setNonceStr(ns)//随机字符串
                    .setMchBillNo(ns)// 商户订单号
                    .setMchId(config.getMchId())//商户号
                    .setWxAppId(config.getAppId())//对应的微信APPID
                    .setSendName("XXX商户")//商户名称
                    .setReOpenId("ohpISuIYhbVVRMxm7ccOop18V4BA")//接收红包用户的openid
                    .setTotalAmount(100)//付款金额  单位分
                    .setTotalNum(1)//红包发放总人数
                    .setWishing("祝福语")//红包祝福语
                    .setClientIp(IpKit.getRealIp(getRequest()))//Ip地址
                    .setActName("XXX活动")//活动名称
                    .setRemark("备注")//备注
                    .setPaternerKey(config.getPaternerKey())//API密钥 商户签名key
                    .build();
            String sendRedPack = RedHbApi.sendRedPack(params, certPath, config.getMchId());
            Map<String, String> xmlToMap = PaymentKit.xmlToMap(sendRedPack);

            String return_code = xmlToMap.get("return_code");
            String result_code = xmlToMap.get("result_code");
            
            if (PaymentKit.codeIsOK(return_code)&& PaymentKit.codeIsOK(result_code)) {
            	renderJson(true);
            } else {
              	renderJson(false);
            }
    }

    /**
     * 微信分裂红包
     *
     * @param wechatRedPack
     * @param request
     * @return
     */
    public void sendGroupRedPack() {
        
            WxPayApiConfig config = getApiConfig();
            WxPayApiConfigKit.setThreadLocalWxPayApiConfig(config);
            String ns = String.valueOf(System.currentTimeMillis());
            Map<String, String> params = ReadHbModle.Builder()
                    .setHbType(ReadHbModle.HbType.DIVIDE)//分裂红包
                    .setPayModel(config.getPayModel())
                    .setNonceStr(ns)//随机字符串
                    .setMchBillNo(ns)// 商户订单号
                    .setMchId(config.getMchId())//商户号
                    .setWxAppId(config.getAppId())//对应的微信APPID
                    .setSendName("XXX商户")//商户名称
                    .setReOpenId("ohpISuIYhbVVRMxm7ccOop18V4BA")//接收红包用户的openid
                    .setTotalAmount(300)//付款金额  单位分
                    .setTotalNum(3)//红包发放总人数
                    .setWishing("祝福语")//红包祝福语
                    .setActName("XXX活动")//活动名称
                    .setRemark("备注")//备注
                    .setAmtType(ReadHbModle.AmtType.ALL_RAND)//红包金额设置方式 ：全部随机
                    .setPaternerKey(config.getPaternerKey())//API密钥 商户签名key
                    .build();
            String sendRedPack = RedHbApi.sendGroupRedPack(params, certPath, config.getMchId());
            Map<String, String> xmlToMap = PaymentKit.xmlToMap(sendRedPack);

            String return_code = xmlToMap.get("return_code");
            String result_code = xmlToMap.get("result_code");
            if (PaymentKit.codeIsOK(return_code)&& PaymentKit.codeIsOK(result_code)) {
            	renderJson(true);
            } else {
              	renderJson(false);
            }
    }

    //根据订单号查询红包状态
    public void getRedPackInfo() {
		renderText(getRedPackInfoByDdh(getPara("treadeno")));
	}
    
    //通过订单号查询红包信息
    public String getRedPackInfoByDdh(String treadeno){
        WxPayApiConfig config = getApiConfig();
        Map<String, String> params = new HashMap<String, String>();
        // 随机字符串
        params.put("nonce_str", System.currentTimeMillis() / 1000 + "");
        // 商户订单号
        params.put("mch_billno", treadeno);
        // 商户号
        params.put("mch_id", config.getMchId());
        // 公众账号ID
        params.put("appid", config.getAppId());
        params.put("bill_type", "MCHT");
        //创建签名
        String sign = PaymentKit.createSign(params, config.getPaternerKey());
        params.put("sign", sign);

        String xmlResult = RedHbApi.getHbInfo(params, certPath, config.getMchId());
        return xmlResult;
    }

}
