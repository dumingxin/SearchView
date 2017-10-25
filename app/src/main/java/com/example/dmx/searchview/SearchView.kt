package com.example.dmx.searchview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View

/**
 * Created by dmx on 17-10-25.
 */
class SearchView : View {
    private lateinit var mPaint: Paint
    private var mViewWidth = 0
    private var mViewHeight = 0

    //当前状态
    private var mCurrentState = State.NONE
    //放大镜和外部圆环
    private lateinit var pathSrarch: Path
    private lateinit var pathCircle: Path
    //测量Path，并截取部分的工具
    private lateinit var mMeasure: PathMeasure
    //默认的动效周期为2s
    private var defaultDuration = 2000L
    //控制各个过程的的动画
    private lateinit var mStartingAnimator: ValueAnimator
    private lateinit var mSearchingAnimator: ValueAnimator
    private lateinit var mEndingAnimator: ValueAnimator
    //动画数值
    private var mAnimatorValue = 0F
    //动效过程监听
    private var mUpdateListener: ValueAnimator.AnimatorUpdateListener? = null
    private var mAnimatorListener: Animator.AnimatorListener? = null
    //用于控制动画状态转换
    private lateinit var mAnimatorHandler: Handler
    //判断搜索是否已经结束
    private var isOver = false
    private var count = 0

    private enum class State {
        NONE, STARTING, SEARCHING, ENDING
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle) {
        initAll()

    }

    private fun initAll() {
        initPaint()
        initPath()
        initListener()
        initHandler()
        initAnimator()
        mCurrentState = State.STARTING
        mStartingAnimator.start()
    }

    fun reset() {
        mCurrentState = State.STARTING
        mStartingAnimator.start()
    }

    private fun initPaint() {
        mPaint = Paint()
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.WHITE
        mPaint.strokeWidth = 15F
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.isAntiAlias = true

    }

    private fun initPath() {
        pathSrarch = Path()
        pathCircle = Path()
        mMeasure = PathMeasure()
        //绘制放大镜圆环
        val rectSrarch = RectF(-50F, -50F, 50F, 50F)
        pathSrarch.addArc(rectSrarch, 45F, 359.9F)
        //绘制外部圆环
        val rectCircle = RectF(-100F, -100F, 100F, 100F)
        pathCircle.addArc(rectCircle, 45F, 359.9F)
        val pos = floatArrayOf(0F, 0F)
        //获取放大镜把手位置
        mMeasure.setPath(pathCircle, false)
        mMeasure.getPosTan(0F, pos, null)
        //绘制放大镜把手
        pathSrarch.lineTo(pos[0], pos[1])
    }

    private fun initListener() {
        mUpdateListener = ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            mAnimatorValue = valueAnimator?.animatedValue as Float
            invalidate()
        }
        mAnimatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                //发送消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0)
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        }
    }

    private fun initHandler() {
        mAnimatorHandler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                when (mCurrentState) {
                    State.STARTING -> {
                        isOver = false
                        mCurrentState = State.SEARCHING
                        mSearchingAnimator.start()
                    }
                    State.SEARCHING -> {
                        if (!isOver) {
                            mSearchingAnimator.start()
                            count++
                            if (count > 2) {
                                isOver = true
                            }
                        } else {
                            mCurrentState = State.ENDING
                            mEndingAnimator.start()
                        }
                    }
                    State.ENDING -> {
                        mCurrentState = State.NONE
                    }
                }
            }
        }
    }

    private fun initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(defaultDuration)
        mSearchingAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(defaultDuration)
        mEndingAnimator = ValueAnimator.ofFloat(1F, 0F).setDuration(defaultDuration)

        mStartingAnimator.addUpdateListener(mUpdateListener)
        mSearchingAnimator.addUpdateListener(mUpdateListener)
        mEndingAnimator.addUpdateListener(mUpdateListener)

        mStartingAnimator.addListener(mAnimatorListener)
        mSearchingAnimator.addListener(mAnimatorListener)
        mEndingAnimator.addListener(mAnimatorListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSearch(canvas)
    }

    private fun drawSearch(canvas: Canvas) {
        mPaint.color = Color.WHITE
        canvas.translate((mViewWidth / 2).toFloat(), (mViewHeight / 2).toFloat())
        canvas.drawColor(Color.parseColor("#0082D7"))
        when (mCurrentState) {
            State.NONE -> canvas.drawPath(pathSrarch, mPaint)
            State.STARTING -> {
                mMeasure.setPath(pathSrarch, false)
                val dst = Path()
                mMeasure.getSegment(mMeasure.length * mAnimatorValue, mMeasure.length, dst, true)
                canvas.drawPath(dst, mPaint)
            }
            State.SEARCHING -> {
                mMeasure.setPath(pathCircle, false)
                val dst = Path()
                val stop = mMeasure.length * mAnimatorValue
                val start = stop - Math.abs(mAnimatorValue - 0.5) * 200F
                mMeasure.getSegment(start.toFloat(), stop, dst, true)
                canvas.drawPath(dst, mPaint)
            }
            State.ENDING -> {
                mMeasure.setPath(pathSrarch, false)
                val dst = Path()
                mMeasure.getSegment(mMeasure.length * mAnimatorValue, mMeasure.length, dst, true)
                canvas.drawPath(dst, mPaint)
            }
        }
    }
}