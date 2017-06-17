package xin.fallen.UsedVehPicTransfer.util;


import xin.fallen.UsedVehPicTransfer.vo.JsonResult;

/**
 * Author: Fallen
 * Date: 2017/3/6
 * Time: 10:31
 * Usage:
 */
public class JsonResultUtil {

    public static JsonResult resDispatcher(Object data) {
        return data == null ? getFailRes() : getSuccessRes(data);
    }

    public static JsonResult resDispatcher(String successMsg, String failMsg, Object data) {
        return data == null ? getFailRes(failMsg) : getSuccessRes(successMsg, data);
    }

    public static JsonResult resDispatcher(int status) {
        return status == 0 ? getFailRes() : getSuccessRes();
    }

    public static JsonResult resDispatcher(boolean status) {
        return status ? getSuccessRes() : getFailRes();
    }

    public static JsonResult resDispatcher(String successMsg, String failMsg, int status) {
        return status == 0 ? getFailRes(failMsg) : getSuccessRes(successMsg);
    }

    public static JsonResult resDispatcher(String successMsg, String failMsg, boolean status) {
        return status ? getSuccessRes(successMsg) : getFailRes(failMsg);
    }

    public static JsonResult resDispatcher(String msg, int status) {
        return status == 0 ? getFailRes(msg) : getSuccessRes(msg);
    }

    private static JsonResult getSuccessRes(String msg, Object data) {
        JsonResult jr = new JsonResult();
        jr.setRes("1");
        jr.setMsg(msg);
        jr.setData(data);
        return jr;
    }

    private static JsonResult getSuccessRes(Object data) {
        JsonResult jr = new JsonResult();
        jr.setRes("1");
        jr.setMsg("操作成功");
        jr.setData(data);
        return jr;
    }

    private static JsonResult getSuccessRes() {
        JsonResult jr = new JsonResult();
        jr.setRes("1");
        jr.setMsg("操作成功");
        return jr;
    }

    private static JsonResult getFailRes(String res) {
        JsonResult jr = new JsonResult();
        jr.setRes("0");
        jr.setMsg(res);
        return jr;
    }

    private static JsonResult getFailRes() {
        JsonResult jr = new JsonResult();
        jr.setRes("0");
        jr.setMsg("操作失败");
        return jr;
    }
}