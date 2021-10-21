import com.android.build.gradle.AbstractAppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile

// Aop配置插件
class AopConfigPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.afterEvaluate {
            DependencyHandler dependencyHandler = project.getDependencies()
            dependencyHandler.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, "org.aspectj:aspectjtools:1.9.6")
            if (project.getExtensions().findByName("android") != null) {
                chooseConfigWay(project.getExtensions().findByName("android"), project)
            }
        }
    }

    // 选择配置方式 因为主工程 和 module他们的变量是不一样的
    void chooseConfigWay(Object targetObject, Project project) {
        if (targetObject instanceof AbstractAppExtension) {
            targetObject.applicationVariants.all { variant ->
                variant.outputs.all { output ->
                    def fullName = ""
                    output.name.tokenize('-').eachWithIndex { token, index ->
                        fullName = fullName + (index == 0 ? token : token.capitalize())
                    }
                    config(fullName, variant, project)
                }
            }
        } else if (targetObject instanceof LibraryExtension) {
            targetObject.libraryVariants.all { variant ->
                variant.outputs.all { output ->
                    def fullName = ""
                    output.name.tokenize('-').eachWithIndex { token, index ->
                        fullName = fullName + (index == 0 ? token : token.capitalize())
                    }
                    config(fullName, variant, project)
                }
            }
        }
    }

    void config(String fullName, BaseVariant variant, Project project) {
        JavaCompile javaCompile = variant.javaCompiler
        MessageHandler handler = new MessageHandler(true)
        javaCompile.doLast {
            String[] javaArgs = ["-showWeaveInfo",
                                 "-1.8",
                                 "-inpath", javaCompile.destinationDir.toString(),
                                 "-aspectpath", javaCompile.classpath.asPath,
                                 "-d", javaCompile.destinationDir.toString(),
                                 "-classpath", javaCompile.classpath.asPath,
                                 "-bootclasspath", project.android.bootClasspath.join(
                    File.pathSeparator)]

            String[] kotlinArgs = ["-showWeaveInfo",
                                   "-1.8",
                                   "-inpath", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                   // asjectj编译器的classpath aspectjtools
                                   "-aspectpath", javaCompile.classpath.asPath,
                                   "-d", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                   "-classpath", javaCompile.classpath.asPath,
                                   "-bootclasspath", project.android.bootClasspath.join(
                    File.pathSeparator)]
            new Main().run(javaArgs, handler)
            new Main().run(kotlinArgs, handler)

            def log = project.logger
            for (IMessage message : handler.getMessages(null, true)) {
                switch (message.getKind()) {
                    case IMessage.ABORT:
                    case IMessage.ERROR:
                    case IMessage.FAIL:
                        println("IMessage.error = ${message.message}")
                        log.error message.message, message.thrown
                        break
                    case IMessage.WARNING:
                    case IMessage.INFO:
                        log.info message.message, message.thrown
                        println("IMessage.INFO = ${message.message}")
                        break
                    case IMessage.DEBUG:
                        log.debug message.message, message.thrown
                        break
                }
            }
        }
    }
}