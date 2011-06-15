package org.yes.cart.web.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.yes.cart.shoppingcart.ShoppingCart;
import org.yes.cart.web.support.util.cookie.CookieTuplizer;
import org.yes.cart.web.support.util.cookie.UnableToObjectizeCookieException;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Shopping cart  filter responsible to restore shopping cart from cookies, if it possible.
 * <p/>
 * User: dogma
 * Date: 2011-May-17
 * Time: 6:13:57 PM
 */
public class ShoppingCartSessionFilter extends AbstractFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartSessionFilter.class);

    private final CookieTuplizer tuplizer;


    /**
     * @param tuplizer                   tuplizer to manage cookie to object to cookie transformation
     */
    public ShoppingCartSessionFilter(final CookieTuplizer tuplizer) {
        super();
        this.tuplizer = tuplizer;
    }


    /**
     * {@inheritDoc}
     */
    public ServletRequest doBefore(final ServletRequest request,
                                   final ServletResponse response) throws IOException, ServletException {


        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final ShoppingCart shoppingCart =  getShoppingCart(request, response);

        if (httpRequest.getSession().isNew()) { // try to restore from cookies
            synchronized (tuplizer) {
                ShoppingCart restoredCart = null;
                try {
                    restoredCart  = tuplizer.toObject(
                                httpRequest.getCookies(),
                                shoppingCart);
                    BeanUtils.copyProperties(restoredCart, shoppingCart);
                } catch (UnableToObjectizeCookieException e) {
                    if(LOG.isWarnEnabled()) {
                        LOG.warn(MessageFormat.format("Cart {0} not restored from cookies", shoppingCart.getGuid()));
                    }
                }
            }
        }
        return request;
    }




    /**
     * {@inheritDoc}
     */
    public void doAfter(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IOException, ServletException {
        // NOTHING
    }


}
