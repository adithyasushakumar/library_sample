package com.samboy.roto;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ANDROID on 6/2/2017.
 */

public class Roto {
    private static Roto mRoto = new Roto();

    private static Context ctx;
    private static Cache cache;
    private Map<Integer,RequestQueue> queues=new HashMap<>();
    private List<OnRotoListener> listeners=new ArrayList<>();
    //    private OnRotoListener onRotoListener;
    private static final Object LOCK=new Object();




    public static void init(Context ctx) {
        Roto.ctx=ctx.getApplicationContext();
        createCache(ctx.getApplicationContext());
    }

    public static Roto getInstance() {
        return mRoto;
    }
    private static void createCache(Context ctx) {
        try {
            if(ctx.getExternalCacheDir().exists()) {
                cache = new DiskBasedCache(ctx.getExternalCacheDir(), 10 * 1024 * 1024);
            } else {
                cache = new DiskBasedCache(ctx.getCacheDir(), 10 * 1024 * 1024);
            }

        } catch (Exception e) {
        }
    }


    private Roto() {

    }

    /*public static void setOnRotoListener(OnRotoListener onRotoListener) {
        synchronized (LOCK) {
            if(mRoto!=null) {
                mRoto.onRotoListener = onRotoListener;
            }
        }

    }*/

    public static boolean callWeb(WebModel model){
        try {
            return Roto.getInstance().call(model);
        }catch (Exception e){}
        return false;
    }

    public static void addOnRotoListener(OnRotoListener onRotoListener) {
        synchronized (LOCK) {
            if(mRoto !=null && mRoto.listeners!=null) {
                mRoto.listeners.add(onRotoListener);
            }
        }

    }

    public static void clear() {
        if(mRoto !=null) {
            mRoto.listeners.clear();
            for (Map.Entry<Integer, RequestQueue> x: mRoto.queues.entrySet()) {
                x.getValue().cancelAll(x.getKey());
            }
            mRoto.queues.clear();
            mRoto.listeners=null;
//            mRoto.onRotoListener=null;
            mRoto.queues=null;
            cache=null;
            ctx=null;
            mRoto =null;
        }
    }
    public static void removeListener(OnRotoListener listener) {
        if(mRoto !=null) {
            synchronized (LOCK) {
                mRoto.listeners.remove(listener);
            }
        }
    }
    public static void removeRequest(int requestId) {
        if(mRoto !=null) {
            synchronized (LOCK) {
                if(mRoto.queues.containsKey(new Integer(requestId))) {
                    mRoto.queues.get(new Integer(requestId)).cancelAll(requestId);
                    mRoto.queues.remove(new Integer(requestId));
                }
            }
        }
    }



    public synchronized boolean call(WebModel model) {
        try {
            synchronized (LOCK) {
                /*if (model.keys.length != model.values.length || db == null) {
                    return false;
                }*/
                Log.e("EXP","Roto");
                if (model.onRotoListener !=null && !listeners.contains(model.onRotoListener)) {
                    listeners.add(model.onRotoListener);
                }
                if (model.cancelAll) {
                    if (queues.containsKey(new Integer(model.requestId))) {
                        queues.get(new Integer(model.requestId)).cancelAll(model.requestId);
                        queues.remove(new Integer(model.requestId));
                    }
                }
                final Map<String, String> params = new HashMap<>();
                for (int i = 0; i < model.keys.length; i++) {
                    if(TextUtils.isEmpty(model.keys[i])) {
                        continue;
                    }
                    if(model.values[i]==null) {
                        model.values[i]="";
                    }
                /*if(model.requestId== Constants.VOLLEY_REQUEST_SYNC_COUNTRY) {
                    Log.e("Roto","key="+model.keys[i]+"  v="+model.values[i]);
                }*/

                    params.put(model.keys[i], model.values[i]);
                }
                final int requestId=model.requestId;


                StringRequest request = new StringRequest(StringRequest.Method.POST, model.url, response -> {
                    synchronized (LOCK) {

//                        if(onRotoListener!=null) {
//                            onRotoListener.onVolleySuccess(response,requestId);
//                        }
                        try {

                            for (OnRotoListener l : listeners) {
                                l.onRotoSucceed(response, requestId);
                            }
                        } catch (Exception e) {

                        }

                    }
                }, error -> {
                    synchronized (LOCK) {

//                        if(onRotoListener!=null) {
//                            onRotoListener.onVolleyError(Utilities.parseVolleyErrorMsg(login_dialog),requestId);
//                        }
                        try {
                            String msg = parseVolleyErrorMsg(error);
                            for (OnRotoListener l : listeners) {
                                l.onRotoFailed(msg, requestId);
                            }
                        } catch (Exception e) {

                        }

                    }
                }) {
                    @Override
                    public RetryPolicy getRetryPolicy() {
                        return super.getRetryPolicy();
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        try {
                            headers.put("userkey", "189u73#HH8jd,d7P./3$@8!qw4e5-b31");
                        } catch (Exception e) {
                        }
                        return headers;
                    }


                };
                RequestQueue requestQueue = queues.get(new Integer(requestId));
                if (requestQueue == null) {
//                    requestQueue = new RequestQueue(cache, new BasicNetwork(new HurlStack()));
                    requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(ctx);

                }
//                requestQueue = com.android.volley.toolbox.Roto.newRequestQueue(ctx);
                request.setTag(requestId);
                requestQueue.add(request);
                if(!queues.containsKey(new Integer(requestId))) {
                    queues.put(new Integer(requestId),requestQueue);
                }
                return true;
            }
        } catch (Exception e) {
            Log.e("EXP","",e);
            try {
                for (OnRotoListener l : listeners) {
                    l.onRotoFailed(("Somthing Wrong"), model.requestId);
                }
            } catch (Exception e1) {

            }
        }

        return false;


    }

    public static String parseVolleyErrorMsg(VolleyError error) {//
        error.printStackTrace();
        try {
            switch (error.networkResponse.statusCode) {
                case 403:
                    return "Connection failed";
                case 502:// Bad Gateway
                case 444://No Response
                case 404:
                    return "Connection error.";//Not found
                case 408://timeout
                case 504:

                    return "Connection timeout.";// Gateway Time-out
                case 413://payload large
                case 429://Too many requests
                case 509://Bandwidth Limit Exceeded

                case 500:
                    return "Server busy.";// Internal Server Error
                default:
                    return "Something went wrong. Please try again.";
            }
        } catch (Exception e) {
        }
        return "Please check your internet connection.";
    }
}
