package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.Task;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/delete")
public class DeleteServlet extends HttpServlet {

    private static Logger logger = LogManager.getLogger(DeleteServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /delete");

        String idStr = req.getParameter("id");
        if (idStr == null)
            throw new ServletException("Error: id parameter missing.");
        Long id = Long.parseLong(idStr);

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Task task = em.find(Task.class, id);
            if(task != null) {
                em.remove(task);
            }
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        } finally {
            em.close();
        }

        // Redirect back to the task list using a relative URL so the browser resolves it
        // against its own address. Redirecting to an absolute path like "/" makes the
        // servlet container build an absolute URL (e.g. http://localhost/) from the request
        // Host header, which breaks behind a proxy such as GitHub Codespaces port forwarding.
        // Pass the URL through encodeRedirectURL so URL-rewriting session tracking still works
        // when cookies are disabled (encodeRedirectURL keeps the URL relative).
        resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
        resp.setHeader("Location", resp.encodeRedirectURL("."));
    }
}
