package net.apcsimple.controlapplication.utility

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextUtility: ApplicationContextAware {

    companion object {
        lateinit var applicationContext: ApplicationContext

        @Throws(BeansException::class)
        fun <T> getBean(requiredType: Class<T>): T {
            return applicationContext.getBean(requiredType)
        }
    }



    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringContextUtility.applicationContext = applicationContext
    }
}