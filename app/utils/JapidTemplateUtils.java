package utils;

import cn.bran.japid.template.RenderResult;
import cn.bran.play.JapidTemplateBase;

/**
 * 注意：只要有可能,请使用def方法解决该问题。 <br>
 * 见GroupController.jsonRightGroupPanel()
 * 
 * 功能：（在controller中）立刻获取指定的tag渲染后的string<br>
 * 
 * 实际需求：浏览器发起一个json请求，期望得到一个json对象，该对象某一属性 保函html代码。<br>
 * 
 * 原因：原思路为定义一个｀t htmlToJson模板，该模板想写成下面这样： {"succ":"true",
 * "html":"`t htmlTag1` `t htmlTag2` ..."}
 * htmlTag1和2是格式良好易于维护的常规tag，但在实际运行中，由于json解析
 * 格式的限制，导致js无法获取正常结果。将htmlTag1修改为如下格式可解决问题 '<div id="aaa">'+ '<a>balabalba</a>'
 * + '</div>' 请注意‘’符号以及尾部的+号。但是这对模板可读性于维护性有一定的损害。由于 上术理由我编写此类。但是我觉得try
 * catch的形式影响japid的封装性，以及 未来可能造成版本兼容性。现在没相处更好的法子希望各位有好主意帮助该进<br>
 * 
 * 用法 sample： 1）String targsResult = templateToStr( new
 * htmlTag1().render(arg...)); 2）如果tag无参则更简单 String targsResult =
 * templateToStr(new htmlTag1()); 3）GroupController.jsonRightGroupPanel()
 * 
 * 2011-11－18 @author guyingjong
 */
public class JapidTemplateUtils {
	public static String templateToStr(JapidTemplateBase jt) {
		return templateToStr(jt.render());
	}

	public static String templateToStr(RenderResult r) {
		StringBuilder content = r.getContent();
		if (content == null)
			return "";
		return content.toString();
	}

	public static String templatesToString(RenderResult... templates) {
		StringBuffer sb = new StringBuffer();
		for (RenderResult r : templates) {
			sb.append(templateToStr(r));
		}
		return sb.toString();
	}

	public static String templatesToString(JapidTemplateBase... templates) {
		StringBuffer sb = new StringBuffer();
		for (JapidTemplateBase tag : templates) {
			sb.append(templateToStr(tag));
		}
		return sb.toString();
	}
}
