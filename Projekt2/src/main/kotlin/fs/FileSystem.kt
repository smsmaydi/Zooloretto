import java.io.File

/**
 * - An Interface for tasks that needs the file system
 */
interface FileSystem {

    /**
     * - creates a directory recursively with the given path
     */
    fun mkdir(path: String): Unit

    /**
     * - lists the contents of a directory with the given path and returns the names of files and directories that are there
     * - throws error if it can't do that
     */
    fun listDir(path: String): List<String>


    /**
     * - returns the contents of a file
     * - throws error if it can't do that
     */
    fun readFile(path: String): String

    /**
     * - writes the content to a file with the given path
     * - throws error if it can't do that
     */
    fun writeFile(path: String, content: String): Unit

    /**
     * - joins the given paths to a single path
     * - e.g input=('/' , 'home' , 'myDir')
     * - e.g output for unix systems -> '/home/myDir'
     */
    fun joinPath(vararg paths: String): String

}


/**
 * A file system impl
 */
class FileSystemImpl : FileSystem {

    override fun mkdir(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    override fun listDir(path: String): List<String> {
        val dir = File(path)
        return dir.list().toList()
    }

    override fun readFile(path: String): String {
        val file = File(path)
        return file.readText()
    }

    override fun writeFile(path: String, content: String) {
        val paths = path.split(File.separator)
        // we have a dir. make sure to create it if it does not exist
        if (paths.size > 1) {
            this.mkdir(paths.subList(0, paths.size - 1).joinToString(separator = File.separator))
        }
        val file = File(path)
        file.writeText(content)
    }

    override fun joinPath(vararg paths: String): String {
        return paths.joinToString(File.separator)
    }

}

