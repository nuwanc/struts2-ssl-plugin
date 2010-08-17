package com.googlecode.sslplugin.interceptors;


//Java API imports
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.URI;

//Commons API imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Struts API imports
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.StrutsStatics;
import com.googlecode.sslplugin.util.RequestUtil;


import com.googlecode.sslplugin.annotation.Secured;




/**
 *
 * @author Nuwan
 */
public class SSLInterceptor extends AbstractInterceptor {


    // configure the logger for this class
    private static Log log = LogFactory.getLog(SSLInterceptor.class);


    private String httpsPort;
    private String httpPort;
    private boolean useAnnotations = true;

    /**
     * Defaults for HTTP and HTTPS ports.  Can be overridden in as a interceptor parm in config file.
     */
    final static int HTTP_PORT = 8080;
    final static int HTTPS_PORT = 8443;

    final static String HTTP_GET = "GET";
    final static String HTTP_POST = "POST";
    final static String SCHEME_HTTP = "http";
    final static String SCHEME_HTTPS = "https";

    /** Creates a new instance of SSLInterceptor */
    public SSLInterceptor() {
        super();
        log.info ("Intializing SSLInterceptor");
    }

    /**
     * Redirect to SSL or non-SSL version of page as indicated by the presence (or absence) of the
     *  @Secure annotation on the action class.
     */
    public String intercept(ActionInvocation invocation) throws Exception {

        // initialize request and response
        final ActionContext context = invocation.getInvocationContext ();
        final HttpServletRequest request =
            (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
        final HttpServletResponse response =
            (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);

        // check scheme
        String scheme = request.getScheme().toLowerCase();

        // check method
        String method = request.getMethod().toUpperCase();


        // if useAnnotations is true check for the annotaion marker in the class level or method level
        // else make every request secure.
        // If the action class/method uses the Secured marker annotation, then see if we need to
        // redirect to the SSL protected version of this page

        Object action = invocation.getAction();
        Method method2 = getActionMethod(action.getClass(), invocation.getProxy().getMethod());


        if ( !isUseAnnotations() || action.getClass().isAnnotationPresent(Secured.class) || method2.isAnnotationPresent(Secured.class) ){


            if ( (HTTP_GET.equals(method) || HTTP_POST.equals(method)) && SCHEME_HTTP.equals(scheme)){

                // initialize https port
                int httpsPort = getHttpsPort() == null? HTTPS_PORT : Integer.parseInt(getHttpsPort());

                URI uri = new URI(SCHEME_HTTPS, null, request.getServerName(),
                    httpsPort, response.encodeRedirectURL(request.getRequestURI()),
                    RequestUtil.buildQueryString(request), null);

                log.info("Going to SSL mode, redirecting to " + uri.toString());

                response.sendRedirect(uri.toString());
                return null;
            }
        }
        // Otherwise, check to see if we need to redirect to the non-SSL version of this page
        else{


            if ((HTTP_GET.equals(method) || HTTP_POST.equals(method)) && SCHEME_HTTPS.equals(scheme)){

                // initialize http port
                int httpPort = getHttpPort() == null? HTTP_PORT : Integer.parseInt(getHttpPort());

                URI uri = new URI(SCHEME_HTTP, null, request.getServerName(),
                    httpPort, response.encodeRedirectURL(request.getRequestURI()),
                    RequestUtil.buildQueryString(request), null);

                log.info("Going to non-SSL mode, redirecting to " + uri.toString());

                response.sendRedirect(uri.toString());
                return null;
            }
        }

        return invocation.invoke();
    }

    // FIXME: This is copied from DefaultActionInvocation but should be exposed through the interface
    protected Method getActionMethod(Class actionClass, String methodName) throws NoSuchMethodException {
        Method method;
        try {
            method = actionClass.getMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException e) {
            // hmm -- OK, try doXxx instead
            try {
                String altMethodName = "do" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                method = actionClass.getMethod(altMethodName, new Class[0]);
            } catch (NoSuchMethodException e1) {
                // throw the original one
                throw e;
            }
        }
        return method;
    }


    public String getHttpsPort() {
        return httpsPort;
    }

    @Inject(value="struts2.sslplugin.httpsPort",required = false)
    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getHttpPort() {
        return httpPort;
    }

    @Inject(value = "struts2.sslplugin.httpPort", required = false)
    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isUseAnnotations() {
        return useAnnotations;
    }

    public void setUseAnnotations(boolean useAnnotations) {
        this.useAnnotations = useAnnotations;
    }

    @Inject(value = "struts2.sslplugin.annotations", required = false)
    public void setAnnotations(String annotations) {
        if (annotations==null) {
            annotations = "true";
        }
        this.useAnnotations = new Boolean(annotations).booleanValue();
    }

}

