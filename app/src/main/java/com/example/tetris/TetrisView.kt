package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class TetrisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val cols = 10
    private val rows = 20
    private val grid = Array(rows) { IntArray(cols) { 0 } }

    private var blockSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    private val bgPaint = Paint().apply { color = Color.parseColor("#101010") }
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#2A2A2A")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 52f
    }

    private val colors = intArrayOf(
        0,
        Color.parseColor("#00BCD4"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#FFEB3B"),
        Color.parseColor("#4CAF50"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#F44336")
    )

    private val shapes = listOf(
        arrayOf(intArrayOf(1, 1, 1, 1)),
        arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),
        arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)),
        arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
        arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
        arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),
        arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1))
    )

    private data class Piece(var matrix: Array<IntArray>, var x: Int, var y: Int, val colorIndex: Int)

    private var currentPiece = randomPiece()
    private var lastDrop = System.currentTimeMillis()
    private var dropIntervalMs = 600L
    private var score = 0
    private var gameOver = false

    init {
        isFocusable = true
        isClickable = true
        post(gameLoop)
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameOver) {
                val now = System.currentTimeMillis()
                if (now - lastDrop >= dropIntervalMs) {
                    stepDown()
                    lastDrop = now
                }
                invalidate()
                postDelayed(this, 16)
            }
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(gameLoop)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val availableHeight = h * 0.85f
        blockSize = min(w / cols.toFloat(), availableHeight / rows)
        offsetX = (w - cols * blockSize) / 2f
        offsetY = h * 0.08f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#121212"))

        canvas.drawRect(
            offsetX,
            offsetY,
            offsetX + cols * blockSize,
            offsetY + rows * blockSize,
            bgPaint
        )

        drawGrid(canvas)
        drawPlacedBlocks(canvas)
        drawPiece(canvas, currentPiece)

        canvas.drawText("Score: $score", offsetX, max(56f, offsetY - 20f), textPaint)

        if (gameOver) {
            val gameOverPaint = Paint(textPaint).apply {
                color = Color.parseColor("#FF5252")
                textSize = 72f
            }
            val restartPaint = Paint(textPaint).apply { textSize = 40f }
            canvas.drawText("Game Over", offsetX + 20f, height / 2f, gameOverPaint)
            canvas.drawText("Tap para reiniciar", offsetX + 20f, height / 2f + 60f, restartPaint)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        for (r in 0..rows) {
            val y = offsetY + r * blockSize
            canvas.drawLine(offsetX, y, offsetX + cols * blockSize, y, borderPaint)
        }
        for (c in 0..cols) {
            val x = offsetX + c * blockSize
            canvas.drawLine(x, offsetY, x, offsetY + rows * blockSize, borderPaint)
        }
    }

    private fun drawPlacedBlocks(canvas: Canvas) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val colorIndex = grid[r][c]
                if (colorIndex != 0) {
                    drawBlock(canvas, c, r, colors[colorIndex])
                }
            }
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Piece) {
        for (r in piece.matrix.indices) {
            for (c in piece.matrix[r].indices) {
                if (piece.matrix[r][c] == 1) {
                    drawBlock(canvas, piece.x + c, piece.y + r, colors[piece.colorIndex])
                }
            }
        }
    }

    private fun drawBlock(canvas: Canvas, col: Int, row: Int, color: Int) {
        if (row < 0) return
        val left = offsetX + col * blockSize
        val top = offsetY + row * blockSize
        val right = left + blockSize
        val bottom = top + blockSize

        val fill = Paint().apply { this.color = color }
        canvas.drawRect(left + 2f, top + 2f, right - 2f, bottom - 2f, fill)
        canvas.drawRect(left + 1f, top + 1f, right - 1f, bottom - 1f, borderPaint)
    }

    private fun stepDown() {
        if (!collision(currentPiece, 0, 1, currentPiece.matrix)) {
            currentPiece.y += 1
        } else {
            mergePiece(currentPiece)
            clearLines()
            currentPiece = randomPiece()
            if (collision(currentPiece, 0, 0, currentPiece.matrix)) {
                gameOver = true
            }
        }
    }

    private fun randomPiece(): Piece {
        val shapeIndex = Random.nextInt(shapes.size)
        val matrix = shapes[shapeIndex].map { it.clone() }.toTypedArray()
        val x = cols / 2 - matrix[0].size / 2
        return Piece(matrix, x, -1, shapeIndex + 1)
    }

    private fun mergePiece(piece: Piece) {
        for (r in piece.matrix.indices) {
            for (c in piece.matrix[r].indices) {
                if (piece.matrix[r][c] == 1) {
                    val boardY = piece.y + r
                    val boardX = piece.x + c
                    if (boardY in 0 until rows && boardX in 0 until cols) {
                        grid[boardY][boardX] = piece.colorIndex
                    }
                }
            }
        }
    }

    private fun clearLines() {
        var linesCleared = 0
        var r = rows - 1
        while (r >= 0) {
            if (grid[r].all { it != 0 }) {
                for (row in r downTo 1) {
                    grid[row] = grid[row - 1].clone()
                }
                grid[0] = IntArray(cols)
                linesCleared++
            } else {
                r--
            }
        }

        if (linesCleared > 0) {
            score += when (linesCleared) {
                1 -> 100
                2 -> 300
                3 -> 500
                else -> 800
            }
            dropIntervalMs = max(150L, dropIntervalMs - (linesCleared * 12L))
        }
    }

    private fun collision(piece: Piece, dx: Int, dy: Int, matrix: Array<IntArray>): Boolean {
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                if (matrix[r][c] == 0) continue
                val newX = piece.x + c + dx
                val newY = piece.y + r + dy

                if (newX < 0 || newX >= cols || newY >= rows) return true
                if (newY >= 0 && grid[newY][newX] != 0) return true
            }
        }
        return false
    }

    private fun rotate(matrix: Array<IntArray>): Array<IntArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        val rotated = Array(cols) { IntArray(rows) }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                rotated[c][rows - 1 - r] = matrix[r][c]
            }
        }
        return rotated
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        if (gameOver) {
            resetGame()
            return true
        }

        val third = width / 3f
        when {
            event.y < height * 0.25f -> {
                val rotated = rotate(currentPiece.matrix)
                if (!collision(currentPiece, 0, 0, rotated)) {
                    currentPiece.matrix = rotated
                }
            }
            event.x < third -> {
                if (!collision(currentPiece, -1, 0, currentPiece.matrix)) {
                    currentPiece.x -= 1
                }
            }
            event.x > third * 2 -> {
                if (!collision(currentPiece, 1, 0, currentPiece.matrix)) {
                    currentPiece.x += 1
                }
            }
            else -> {
                while (!collision(currentPiece, 0, 1, currentPiece.matrix)) {
                    currentPiece.y += 1
                }
                stepDown()
                lastDrop = System.currentTimeMillis()
            }
        }

        invalidate()
        return true
    }

    private fun resetGame() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                grid[r][c] = 0
            }
        }
        score = 0
        dropIntervalMs = 600L
        gameOver = false
        currentPiece = randomPiece()
        lastDrop = System.currentTimeMillis()
        post(gameLoop)
        invalidate()
    }
}
