package teamextension;

import burp.IHttpService;
import burp.IInterceptedProxyMessage;
import burp.IProxyListener;

public class ProxyListener implements IProxyListener {

    private SharedValues sharedValues;

    public ProxyListener(SharedValues sharedValues) {
        this.sharedValues = sharedValues;
    }

    public void processProxyMessage(boolean isResponse,
                                    IInterceptedProxyMessage iInterceptedProxyMessage) {
        IHttpService httpService = iInterceptedProxyMessage.getMessageInfo().getHttpService();
        if ("burptcmessage".equalsIgnoreCase(iInterceptedProxyMessage.getMessageInfo().getHttpService().getHost())) {
            this.sharedValues.getCallbacks().printOutput("got custom link " +
                    "request");
            sharedValues.getCallbacks().issueAlert("This host created a custom repeater payload. If you did not paste this yourself " +
                    "or clicked on a link you should leave that site.");
            iInterceptedProxyMessage.getMessageInfo().setHttpService(this.sharedValues.getCallbacks().getHelpers().buildHttpService(
                    "127.0.0.1", this.sharedValues.getInnerServer().getSocket().getLocalPort(), httpService.getProtocol()));
        } else if (!isResponse && this.sharedValues.getClient().isConnected() && this.sharedValues.getBurpPanel().inRoom()) {
            if (this.sharedValues.getBurpPanel().getShareAllRequestsSetting() ||
                    this.sharedValues.getCallbacks().isInScope(this.sharedValues.getCallbacks().getHelpers().analyzeRequest(iInterceptedProxyMessage.getMessageInfo()).getUrl())) {
                HttpRequestResponse httpRequestResponse = new HttpRequestResponse(iInterceptedProxyMessage.getMessageInfo());
                BurpTCMessage burpMessage = new BurpTCMessage(httpRequestResponse, MessageType.BURP_MESSAGE, null);
                this.sharedValues.getClient().sendMessage(burpMessage);
            }
            if (this.sharedValues.getBurpPanel().getShareCookiesSetting()) {
                this.sharedValues.shareNewCookies();
            }
        }
    }

}
