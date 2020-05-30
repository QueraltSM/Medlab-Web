/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.Carts;

import controllers.FrontCommand;
import ejbs.LogFacade;
import ejbs.CartFacade;
import ejbs.CartitemsFacade;
import ejbs.UsersFacade;
import entities.Cart;
import entities.Cartitems;
import entities.Log;
import entities.Users;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author QSM
 */
public class ModifyCartCommand extends FrontCommand {

    private LogFacade log;
    private CartFacade cartsDB;
    private HttpSession session;
    private UsersFacade usersDB;
    private CartitemsFacade cartitemsDB;

    private void storeBook() {
        long userID = Long.parseLong(String.valueOf(session.getAttribute("userID")));
        Users user = usersDB.find(userID);
        Cart cart = cartsDB.findCartByUserID(user).get(0);
        long bookID = Long.parseLong(String.valueOf(request.getParameter("id")));
        long itemsID = cartitemsDB.findAll().size() + 1;

        Cartitems items = new Cartitems();
        items.setBookid(bookID);
        items.setCartid(cart);

        boolean entered = false;

        for (Cartitems c : cartitemsDB.findAll()) {
            if (c.getBookid() == bookID && c.getCartid().getId().equals(cart.getId())) {
                System.out.println("edit");
                items.setId(c.getId());
                items.setQuantity(c.getQuantity() + 1);
                cartitemsDB.edit(items);
                entered = true;
            }
        }

        if (!entered) {
            System.out.println("create");
            items.setId(itemsID);
            items.setQuantity(1);
            cartitemsDB.create(items);
        }

    }

    private void createCart() {
        long userID = Long.parseLong(String.valueOf(session.getAttribute("userID")));
        Users user = usersDB.find(userID);
        List<Cart> carts = cartsDB.findCartByUserID(user);
        if (carts.isEmpty()) {
            Cart cart = new Cart();
            long cartID = cartsDB.findAll().size() + 1;
            cart.setId(cartID);
            cart.setUserid(user);
            cartsDB.create(cart);
        }
    }

    private void removeBook() {
        long userID = Long.parseLong(String.valueOf(session.getAttribute("userID")));
        Users user = usersDB.find(userID);
        Cart cart = cartsDB.findCartByUserID(user).get(0);
        long bookID = Long.parseLong(String.valueOf(request.getParameter("id")));
        long itemsID = cartitemsDB.findAll().size()+1;
        Cartitems items = new Cartitems();
        items.setBookid(bookID);
        items.setCartid(cart);
        cartitemsDB.findAll().stream().filter((c) -> (c.getBookid() == bookID && c.getCartid().getId().equals(cart.getId()))).forEach((c) -> {
            cartitemsDB.remove(c);
        });
    }

    private void decreaseQuantity() {
        long userID = Long.parseLong(String.valueOf(session.getAttribute("userID")));
        Users user = usersDB.find(userID);
        Cart cart = cartsDB.findCartByUserID(user).get(0);
        long bookID = Long.parseLong(String.valueOf(request.getParameter("id")));
        long itemsID = cartitemsDB.findAll().size()+1;
        Cartitems items = new Cartitems();
        items.setBookid(bookID);
        items.setCartid(cart);
        cartitemsDB.findAll().stream().filter((c) -> (c.getBookid() == bookID && c.getCartid().getId().equals(cart.getId()))).map((c) -> {
            items.setId(c.getId());
            return c;
        }).map((c) -> {
            items.setQuantity(c.getQuantity()-1);
            return c;
        }).forEach((_item) -> {
            if (items.getQuantity() == 0) removeBook();
            else cartitemsDB.edit(items);
        });
    }

    private void addBook() {
        long userID = Long.parseLong(String.valueOf(session.getAttribute("userID")));
        Users user = usersDB.find(userID);
        Cart cart = cartsDB.findCartByUserID(user).get(0);
        long bookID = Long.parseLong(String.valueOf(request.getParameter("id")));
        long itemsID = cartitemsDB.findAll().size()+1;
        Cartitems items = new Cartitems();
        items.setBookid(bookID);
        items.setCartid(cart);
        cartitemsDB.findAll().stream().filter((c) -> (c.getBookid() == bookID && c.getCartid().getId().equals(cart.getId()))).map((c) -> {
            items.setId(c.getId());
            return c;
        }).map((c) -> {
            items.setQuantity(c.getQuantity()+1);
            return c;
        }).forEach((_item) -> {
            cartitemsDB.edit(items);
        });
    }

    @Override
    public void process() {
        try {
            session = request.getSession(true);
            log = (LogFacade) InitialContext.doLookup("java:global/Medlab/Medlab-ejb/LogFacade!ejbs.LogFacade");
            Log log1 = new Log();
            long id = 1;
            if (!log.findAll().isEmpty()) {
                id = log.findAll().size() + 1;
            }
            log1.setId(id);
            log1.setDate(new Date());
            log1.setEjbs("ModifyCartsCommand:process()");
            log.create(log1);
            cartsDB = (CartFacade) InitialContext.doLookup("java:global/Medlab/Medlab-ejb/CartFacade!ejbs.CartFacade");
            usersDB = (UsersFacade) InitialContext.doLookup("java:global/Medlab/Medlab-ejb/UsersFacade!ejbs.UsersFacade");
            cartitemsDB = (CartitemsFacade) InitialContext.doLookup("java:global/Medlab/Medlab-ejb/CartitemsFacade!ejbs.CartitemsFacade");
            switch (request.getParameter("action")) {
                case "add":
                    addBook();
                    break;
                case "decrease":
                    decreaseQuantity();
                    break;
                case "remove":
                    removeBook();
                    break;
            }
            ShowCartCommand command = new ShowCartCommand();
            command.init(context, request, response);
            command.process();
        } catch (NamingException ex) {
            Logger.getLogger(ModifyCartCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}