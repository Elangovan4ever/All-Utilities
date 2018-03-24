Imports System.Runtime.InteropServices

Public Class ElaCustomComboBox
    Inherits ComboBox

    Public Const WM_NCPAINT As Integer = &H85

    Private mCurrentBorderColor As Color
    Private mBorderColor As Color
    Private mBorderColorFocus As Color
    Private mBorderColorMouseEnter As Color
    Private mBorderWidth As Integer

    Private mCurrentArrowSquareColor As Color
    Private mCurrentArrowTriangleColor As Color
    Private mArrowSquareColor As Color
    Private mArrowTriangleColor As Color
    Private mArrowSquareColorFocus As Color
    Private mArrowTriangleColorFocus As Color
    Private mArrowSquareColorMouseEnter As Color
    Private mArrowTriangleColorMouseEnter As Color

    Public Property BorderColor As Color
        Get
            Return mBorderColor
        End Get
        Set(ByVal Value As Color)
            mBorderColor = Value
        End Set
    End Property

    Public Property BorderColorFocus As Color
        Get
            Return mBorderColorFocus
        End Get
        Set(ByVal Value As Color)
            mBorderColorFocus = Value
        End Set
    End Property

    Public Property BorderColorMouseEnter As Color
        Get
            Return mBorderColorMouseEnter
        End Get
        Set(ByVal Value As Color)
            mBorderColorMouseEnter = Value
        End Set
    End Property

    Public Property BorderWidth As Integer
        Get
            Return mBorderWidth
        End Get
        Set(ByVal Value As Integer)
            mBorderWidth = Value
        End Set
    End Property

    Public Property ArrowSquareColor As Color
        Get
            Return mArrowSquareColor
        End Get
        Set(ByVal Value As Color)
            mArrowSquareColor = Value
        End Set
    End Property

    Public Property ArrowTriangleColor As Color
        Get
            Return mArrowTriangleColor
        End Get
        Set(ByVal Value As Color)
            mArrowTriangleColor = Value
        End Set
    End Property

    Public Property ArrowSquareColorFocus As Color
        Get
            Return mArrowSquareColorFocus
        End Get
        Set(ByVal Value As Color)
            mArrowSquareColorFocus = Value
        End Set
    End Property

    Public Property ArrowTriangleColorFocus As Color
        Get
            Return mArrowTriangleColorFocus
        End Get
        Set(ByVal Value As Color)
            mArrowTriangleColorFocus = Value
        End Set
    End Property

    Public Property ArrowSquareColorMouseEnter As Color
        Get
            Return mArrowSquareColorMouseEnter
        End Get
        Set(ByVal Value As Color)
            mArrowSquareColorMouseEnter = Value
        End Set
    End Property

    Public Property ArrowTriangleColorMouseEnter As Color
        Get
            Return mArrowTriangleColorMouseEnter
        End Get
        Set(ByVal Value As Color)
            mArrowTriangleColorMouseEnter = Value
        End Set
    End Property

    Public Sub New()
        MyBase.New()

        'This call is required by the Windows Form Designer.
        InitializeComponent()

        'Add any initialization after the InitializeComponent() call
        Me.BorderColor = Color.DeepSkyBlue
        Me.BorderColorFocus = Color.Orange
        Me.mBorderColorMouseEnter = Color.Green
        mCurrentBorderColor = Me.BorderColor

        Me.mArrowSquareColor = Color.DeepSkyBlue
        Me.mArrowTriangleColor = Color.Gray
        Me.mArrowSquareColorFocus = Color.Orange
        Me.mArrowTriangleColorFocus = Color.Gray
        Me.mArrowSquareColorMouseEnter = Color.Green
        Me.mArrowTriangleColorMouseEnter = Color.White
        mCurrentArrowSquareColor = Me.mArrowSquareColor
        mCurrentArrowTriangleColor = Me.mArrowTriangleColor

        Me.BorderWidth = 4

    End Sub


    <Flags()>
    Private Enum RedrawWindowFlags As UInteger
        Invalidate = &H1
        InternalPaint = &H2
        [Erase] = &H4
        Validate = &H8
        NoInternalPaint = &H10
        NoErase = &H20
        NoChildren = &H40
        AllChildren = &H80
        UpdateNow = &H100
        EraseNow = &H200
        Frame = &H400
        NoFrame = &H800
    End Enum

    <DllImport("User32.dll")>
    Public Shared Function GetWindowDC(ByVal hWnd As IntPtr) As IntPtr
    End Function

    <DllImport("user32.dll")>
    Private Shared Function ReleaseDC(ByVal hWnd As IntPtr, ByVal hDC As IntPtr) As Boolean
    End Function

    <DllImport("user32.dll")>
    Private Shared Function RedrawWindow(hWnd As IntPtr, lprcUpdate As IntPtr, hrgnUpdate As IntPtr, flags As RedrawWindowFlags) As Boolean
    End Function

    Protected Overrides Sub OnResize(e As System.EventArgs)
        MyBase.OnResize(e)
        RedrawWindow(Me.Handle, IntPtr.Zero, IntPtr.Zero, RedrawWindowFlags.Frame Or RedrawWindowFlags.UpdateNow Or RedrawWindowFlags.Invalidate)
    End Sub

    Protected Overrides Sub WndProc(ByRef m As Message)
        MyBase.WndProc(m)

        If m.Msg = &HF Then

            Dim g As Graphics = Me.CreateGraphics

            g.FillRectangle(New SolidBrush(Color.White), Me.ClientRectangle)
            g.DrawRectangle(New Pen(Me.mCurrentBorderColor, Me.mBorderWidth), Me.ClientRectangle)

            Dim pth As Drawing2D.GraphicsPath = New Drawing2D.GraphicsPath()
            Dim TopLeft As PointF = New PointF(Me.Width - 16, (Me.Height - 8) / 2)
            Dim TopRight As PointF = New PointF(Me.Width - 7, (Me.Height - 8) / 2)
            Dim Bottom As PointF = New PointF(Me.Width - 11, (Me.Height + 6) / 2)
            pth.AddLine(TopLeft, TopRight)
            pth.AddLine(TopRight, Bottom)

            Dim Rect As Rectangle = New Rectangle(Me.Width - 19, 3, 16, Me.Height - 6)
            g.FillRectangle(New SolidBrush(Me.mCurrentArrowSquareColor), Rect)

            Dim ArrowBrush As Brush = New SolidBrush(Me.mCurrentArrowTriangleColor)
            g.SmoothingMode = Drawing2D.SmoothingMode.HighQuality

            If Me.DroppedDown Then
                ArrowBrush = New SolidBrush(SystemColors.HighlightText)
            End If

            g.FillPath(ArrowBrush, pth)
        End If

    End Sub

    Protected Overrides Sub OnMouseEnter(ByVal e As System.EventArgs)
        MyBase.OnMouseEnter(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.mBorderColorMouseEnter
        Me.mCurrentArrowSquareColor = Me.mArrowSquareColorMouseEnter
        Me.mCurrentArrowTriangleColor = Me.mArrowTriangleColorMouseEnter
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseLeave(ByVal e As System.EventArgs)
        MyBase.OnMouseLeave(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.mBorderColor
        Me.mCurrentArrowSquareColor = Me.mArrowSquareColor
        Me.mCurrentArrowTriangleColor = Me.mArrowTriangleColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnGotFocus(ByVal e As System.EventArgs)
        MyBase.OnGotFocus(e)
        Me.mCurrentBorderColor = Me.mBorderColorFocus
        Me.mCurrentArrowSquareColor = Me.mArrowSquareColorFocus
        Me.mCurrentArrowTriangleColor = Me.mArrowTriangleColorFocus
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnLostFocus(ByVal e As System.EventArgs)
        MyBase.OnLostFocus(e)
        Me.mCurrentBorderColor = Me.mBorderColor
        Me.mCurrentArrowSquareColor = Me.mArrowSquareColor
        Me.mCurrentArrowTriangleColor = Me.mArrowTriangleColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseHover(ByVal e As System.EventArgs)
        MyBase.OnMouseHover(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.mBorderColorMouseEnter
        Me.mCurrentArrowSquareColor = Me.mArrowSquareColorMouseEnter
        Me.mCurrentArrowTriangleColor = Me.mArrowTriangleColorMouseEnter
        Me.Invalidate()
    End Sub

    'UserControl1 overrides dispose to clean up the component list.
    Protected Overloads Overrides Sub Dispose(ByVal disposing As Boolean)
        If disposing Then
            If Not (components Is Nothing) Then
                components.Dispose()
            End If
        End If
        MyBase.Dispose(disposing)
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        components = New System.ComponentModel.Container()
    End Sub

    Private Sub myComboBox_KeyUp(ByVal sender As Object, ByVal e As System.Windows.Forms.KeyEventArgs) Handles MyBase.KeyUp
        Dim tmpcombo As System.Windows.Forms.ComboBox
        tmpcombo = CType(sender, System.Windows.Forms.ComboBox)
        AutoComplete_KeyUp(tmpcombo, e)
    End Sub


    Private Sub AutoComplete_KeyUp(ByVal cbo As ComboBox, ByVal e As KeyEventArgs)
        Dim sTypedText As String
        Dim iFoundIndex As Integer
        Dim sFoundText As String
        Dim sAppendText As String
        Select Case e.KeyCode
            Case Keys.Back, Keys.Left, Keys.Right, Keys.Up, Keys.Delete, Keys.Down, Keys.Home, Keys.End, Keys.ShiftKey, Keys.ControlKey
                Return
        End Select

        sTypedText = cbo.Text
        iFoundIndex = cbo.FindString(sTypedText)

        If iFoundIndex >= 0 Then
            sFoundText = cbo.Items(iFoundIndex)
            sAppendText = sFoundText.Substring(sTypedText.Length)
            cbo.Text = sTypedText & sAppendText
            cbo.SelectionStart = sTypedText.Length
            cbo.SelectionLength = sAppendText.Length
        End If
    End Sub



End Class

