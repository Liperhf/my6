@file:Suppress("FunctionName", "SameParameterValue")

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    // Init GLFW
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
    glfwDefaultWindowHints()
    // Use compatibility profile to enable fixed-function pipeline (immediate mode)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)

    val width = 1000
    val height = 700
    val window = glfwCreateWindow(width, height, "3D Letter K - OpenGL", NULL, NULL)
        ?: throw RuntimeException("Failed to create GLFW window")

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    GL.createCapabilities()

    // State
    var projection = Projection.OXY
    val model = Mat4.identity()

    // Controls
    glfwSetKeyCallback(window) { _, key, _, action, _ ->
        if (action != GLFW_PRESS && action != GLFW_REPEAT) return@glfwSetKeyCallback
        when (key) {
            GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true)
            GLFW_KEY_1 -> projection = Projection.OXY
            GLFW_KEY_2 -> projection = Projection.OXZ
            GLFW_KEY_3 -> projection = Projection.OYZ

            GLFW_KEY_KP_ADD, GLFW_KEY_EQUAL -> model.scale(1.05f)
            GLFW_KEY_KP_SUBTRACT, GLFW_KEY_MINUS -> model.scale(0.95f)

            GLFW_KEY_LEFT -> model.translate(-0.1f, 0f, 0f)
            GLFW_KEY_RIGHT -> model.translate(0.1f, 0f, 0f)
            GLFW_KEY_UP -> model.translate(0f, 0.1f, 0f)
            GLFW_KEY_DOWN -> model.translate(0f, -0.1f, 0f)

            GLFW_KEY_Z -> model.rotate(5f, 0f, 0f, 1f)
            GLFW_KEY_X -> model.rotate(5f, 1f, 0f, 0f)
            GLFW_KEY_C -> model.rotate(5f, 0f, 1f, 0f)
        }
    }

    // GL setup
    glClearColor(0.1f, 0.12f, 0.15f, 1f)
    glEnable(GL_LINE_SMOOTH)
    glLineWidth(2f)
    glDisable(GL_DEPTH_TEST) // for orthographic projections lines are fine without depth

    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT)

        // Setup projection
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        when (projection) {
            Projection.OXY -> glOrtho(-2.0, 2.0, -1.5, 1.5, -10.0, 10.0)
            Projection.OXZ -> glOrtho(-2.0, 2.0, -1.5, 1.5, -10.0, 10.0)
            Projection.OYZ -> glOrtho(-2.0, 2.0, -1.5, 1.5, -10.0, 10.0)
        }

        // View matrix to pick plane
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()
        when (projection) {
            Projection.OXY -> {}
            Projection.OXZ -> glRotatef(90f, 1f, 0f, 0f)
            Projection.OYZ -> glRotatef(90f, 0f, 1f, 0f)
        }

        // Apply model matrix
        glMultMatrixf(model.asFloatBuffer())

        // Draw axes
        drawAxes()

        // Draw wireframe letter K
        drawLetterK()

        // Update window title with current model matrix
        glfwSetWindowTitle(window, "3D Letter K - ${projection.name}  |  M=${model.pretty()}  |  Controls: 1/OXY 2/OXZ 3/OYZ, +/- scale, arrows move, X/Y/Z rotate")

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}

private fun drawAxes() {
    glBegin(GL_LINES)
    // X - red
    glColor3f(1f, 0f, 0f); glVertex3f(-10f, 0f, 0f); glVertex3f(10f, 0f, 0f)
    // Y - green
    glColor3f(0f, 1f, 0f); glVertex3f(0f, -10f, 0f); glVertex3f(0f, 10f, 0f)
    // Z - blue
    glColor3f(0f, 0f, 1f); glVertex3f(0f, 0f, -10f); glVertex3f(0f, 0f, 10f)
    glEnd()
}

private fun drawLetterK() {
    glColor3f(1f, 1f, 0.2f)
    glBegin(GL_LINES)
    // A simple 3D frame model of letter K built from line segments
    // Vertical spine
    line(-0.5f, -1f, 0f, -0.5f, 1f, 0f)
    // Upper diagonal
    line(-0.5f, 0f, 0f, 0.6f, 1f, 0f)
    // Lower diagonal
    line(-0.5f, 0f, 0f, 0.6f, -1f, 0f)

    // Give it a bit of depth by duplicating slightly in Z and connecting edges
    val dz = 0.2f
    // back layer
    line(-0.5f, -1f, -dz, -0.5f, 1f, -dz)
    line(-0.5f, 0f, -dz, 0.6f, 1f, -dz)
    line(-0.5f, 0f, -dz, 0.6f, -1f, -dz)
    // connect layers
    line(-0.5f, -1f, 0f, -0.5f, -1f, -dz)
    line(-0.5f, 1f, 0f, -0.5f, 1f, -dz)
    line(0.6f, 1f, 0f, 0.6f, 1f, -dz)
    line(0.6f, -1f, 0f, 0.6f, -1f, -dz)
    line(-0.5f, 0f, 0f, -0.5f, 0f, -dz)
    glEnd()
}

private fun line(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
    glVertex3f(x1, y1, z1)
    glVertex3f(x2, y2, z2)
}

private enum class Projection { OXY, OXZ, OYZ }

// Minimal 4x4 matrix utility suitable for fixed-function OpenGL
private class Mat4(private val m: FloatArray) {
    fun asFloatBuffer() = MemoryStack.stackPush().use { stack ->
        val fb = stack.mallocFloat(16)
        fb.put(m).flip()
        fb
    }

    fun scale(s: Float) = postMultiply(scaleMatrix(s, s, s))
    fun translate(x: Float, y: Float, z: Float) = postMultiply(translateMatrix(x, y, z))
    fun rotate(angleDeg: Float, x: Float, y: Float, z: Float) = postMultiply(rotateMatrix(angleDeg, x, y, z))

    fun pretty(): String = buildString {
        append("[")
        for (i in 0 until 4) {
            if (i > 0) append(";")
            for (j in 0 until 4) {
                if (j > 0) append(",")
                append(String.format("%.2f", m[j * 4 + i]))
            }
        }
        append("]")
    }

    private fun postMultiply(b: FloatArray) {
        val res = FloatArray(16)
        for (row in 0 until 4) for (col in 0 until 4) {
            var sum = 0f
            for (k in 0 until 4) sum += m[k * 4 + row] * b[col * 4 + k]
            res[col * 4 + row] = sum
        }
        System.arraycopy(res, 0, m, 0, 16)
    }

    companion object {
        fun identity() = Mat4(floatArrayOf(
            1f,0f,0f,0f,
            0f,1f,0f,0f,
            0f,0f,1f,0f,
            0f,0f,0f,1f
        ))
    }
}

private fun translateMatrix(x: Float, y: Float, z: Float) = floatArrayOf(
    1f,0f,0f,0f,
    0f,1f,0f,0f,
    0f,0f,1f,0f,
    x, y, z, 1f
)

private fun scaleMatrix(x: Float, y: Float, z: Float) = floatArrayOf(
    x,0f,0f,0f,
    0f,y,0f,0f,
    0f,0f,z,0f,
    0f,0f,0f,1f
)

private fun rotateMatrix(angleDeg: Float, ax: Float, ay: Float, az: Float): FloatArray {
    val a = Math.toRadians(angleDeg.toDouble()).toFloat()
    val c = cos(a); val s = sin(a)
    val len = kotlin.math.sqrt((ax*ax + ay*ay + az*az).toDouble()).toFloat().let { if (it == 0f) 1f else it }
    val x = ax/len; val y = ay/len; val z = az/len
    val omc = 1f - c
    return floatArrayOf(
        c + x*x*omc,     y*x*omc + z*s, z*x*omc - y*s, 0f,
        x*y*omc - z*s,   c + y*y*omc,   z*y*omc + x*s, 0f,
        x*z*omc + y*s,   y*z*omc - x*s, c + z*z*omc,   0f,
        0f,              0f,            0f,            1f
    )
}