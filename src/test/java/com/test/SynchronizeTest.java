package com.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.hibernate.Hibernate;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.db.model.AbstractCatalogableEntity;
import com.db.model.CatalogVersion;
import com.db.model.Product;
import com.test.config.TestConfig;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class })
@ContextConfiguration(classes = { TestConfig.class })
@TransactionConfiguration(transactionManager = "transactionManager")
public class SynchronizeTest extends AbstractJUnit4SpringContextTests {

    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager entityManager;

    @Test
    @Transactional
    public void testSync() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

        final Product source = entityManager.find(Product.class, 20l);

        final Product target = (Product) synchronize(source);

        assertNotNull(target);
    }

    private AbstractCatalogableEntity synchronize(AbstractCatalogableEntity sourceEntity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

        AbstractCatalogableEntity targetEntity = null;
        
        try {
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            mapperFactory.registerObjectFactory(new AbstractCatalogableEntityFactory(), TypeFactory.valueOf(AbstractCatalogableEntity.class),
                                                TypeFactory.valueOf(AbstractCatalogableEntity.class));

            mapperFactory.classMap(Hibernate.getClass(sourceEntity), Hibernate.getClass(sourceEntity))
            //.customize(new AbstractCatalogableEntityMapper<AbstractCatalogableEntity>())
            .exclude("pk").exclude("catalogVersion").byDefault().register();

            MapperFacade mapperFacade = mapperFactory.getMapperFacade();
            targetEntity = mapperFacade.map(sourceEntity, (Class<AbstractCatalogableEntity>) Hibernate.getClass(sourceEntity));

            entityManager.merge(targetEntity);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return targetEntity;
    }

    public static class AbstractCatalogableEntityMapper<A extends AbstractCatalogableEntity> extends CustomMapper<A, A> {

        /*
         * (non-Javadoc)
         * @see ma.glasnost.orika.CustomMapper#mapAtoB(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
         */
        @Override
        public void mapAtoB(A a, A b, MappingContext context) {

            super.mapAtoB(a, b, context);
        }

        /*
         * (non-Javadoc)
         * @see ma.glasnost.orika.CustomMapper#mapBtoA(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
         */
        @Override
        public void mapBtoA(A b, A a, MappingContext context) {
            mapAtoB(a, b, context);
        }
    }
    
    class AbstractCatalogableEntityFactory implements ObjectFactory<AbstractCatalogableEntity> {

        @Override
        public AbstractCatalogableEntity create(Object obj, MappingContext mappingContext) {

            final AbstractCatalogableEntity source = (AbstractCatalogableEntity) obj;

            final CatalogVersion onlineCatalogVersion = entityManager.find(CatalogVersion.class, 11l);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("catalogVersion", onlineCatalogVersion);

            try {
                
                String findByCatalogVersionOnline = "from " + Hibernate.getClass(source).getCanonicalName() + " where catalogVersion=:catalogVersion";

                TypedQuery<AbstractCatalogableEntity> q = entityManager.createQuery(findByCatalogVersionOnline, AbstractCatalogableEntity.class);
                q.setParameter("catalogVersion", onlineCatalogVersion);

                AbstractCatalogableEntity onlineEntity = null;
                try {
                    q.getSingleResult();
                } catch (javax.persistence.NoResultException nre) {
                    onlineEntity = source.getClass().newInstance();
                    onlineEntity.setCatalogVersion(onlineCatalogVersion);
                }

                if (onlineEntity != null) {
                    return onlineEntity;
                }

                Type<?> destinationType = mappingContext.getResolvedDestinationType();
                onlineEntity = (AbstractCatalogableEntity) destinationType.getRawType().newInstance();

                onlineEntity.setCatalogVersion(onlineCatalogVersion);

                return onlineEntity;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }
}
