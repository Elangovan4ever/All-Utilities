Imports System.Drawing
Imports System.Runtime.InteropServices
Imports System.Windows.Forms

Public Class ElaCustomDateTimePicker
    Inherits DateTimePicker

    Public Const WM_NCPAINT As Integer = &H85

    Private mCurrentBorderColor As Color
    Private mBorderColor As Color
    Private mBorderColorFocus As Color
    Private mBorderColorMouseEnter As Color
    Private mBorderThickness As BorderThicknessEnum

    Private mCurrentArrowSquareColor As Color
    Private mCurrentArrowTriangleColor As Color
    Private mArrowSquareColor As Color
    Private mArrowTriangleColor As Color
    Private mArrowSquareColorFocus As Color
    Private mArrowTriangleColorFocus As Color
    Private mArrowSquareColorMouseEnter As Color
    Private mArrowTriangleColorMouseEnter As Color

    Enum BorderThicknessEnum
        Normal = 2
        Thick = 4
        Thicker = 8
    End Enum

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

    Public Property BorderThickness As BorderThicknessEnum
        Get
            Return mBorderThickness
        End Get
        Set(ByVal Value As BorderThicknessEnum)
            mBorderThickness = Value
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
        Me.BorderColorMouseEnter = Color.Green

        Me.ArrowSquareColor = Color.DeepSkyBlue
        Me.ArrowTriangleColor = Color.Gray
        Me.ArrowSquareColorFocus = Color.Orange
        Me.ArrowTriangleColorFocus = Color.Gray
        Me.ArrowSquareColorMouseEnter = Color.Green
        Me.ArrowTriangleColorMouseEnter = Color.White

        Me.BorderThickness = BorderThicknessEnum.Thick

        mCurrentBorderColor = Me.mBorderColor
        mCurrentArrowSquareColor = Me.mArrowSquareColor
        mCurrentArrowTriangleColor = Me.mArrowTriangleColor

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

            g.DrawRectangle(New Pen(Me.mCurrentBorderColor, Me.BorderThickness), Me.ClientRectangle)


        End If

    End Sub

    Protected Overrides Sub OnMouseEnter(ByVal e As System.EventArgs)
        MyBase.OnMouseEnter(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColorMouseEnter
        Me.mCurrentArrowSquareColor = Me.ArrowSquareColorMouseEnter
        Me.mCurrentArrowTriangleColor = Me.ArrowTriangleColorMouseEnter
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseLeave(ByVal e As System.EventArgs)
        MyBase.OnMouseLeave(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColor
        Me.mCurrentArrowSquareColor = Me.ArrowSquareColor
        Me.mCurrentArrowTriangleColor = Me.ArrowTriangleColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnGotFocus(ByVal e As System.EventArgs)
        MyBase.OnGotFocus(e)
        Me.mCurrentBorderColor = Me.BorderColorFocus
        Me.mCurrentArrowSquareColor = Me.ArrowSquareColorFocus
        Me.mCurrentArrowTriangleColor = Me.ArrowTriangleColorFocus
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnLostFocus(ByVal e As System.EventArgs)
        MyBase.OnLostFocus(e)
        Me.mCurrentBorderColor = Me.BorderColor
        Me.mCurrentArrowSquareColor = Me.ArrowSquareColor
        Me.mCurrentArrowTriangleColor = Me.ArrowTriangleColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseHover(ByVal e As System.EventArgs)
        MyBase.OnMouseHover(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColorMouseEnter
        Me.mCurrentArrowSquareColor = Me.ArrowSquareColorMouseEnter
        Me.mCurrentArrowTriangleColor = Me.ArrowTriangleColorMouseEnter
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
        Me.SuspendLayout()
        Me.ResumeLayout(False)

    End Sub

    'Private Sub myComboBox_KeyUp(ByVal sender As Object, ByVal e As System.Windows.Forms.KeyEventArgs) Handles MyBase.KeyUp
    '    Dim tmpcombo As System.Windows.Forms.ComboBox
    '    tmpcombo = CType(sender, System.Windows.Forms.ComboBox)
    '    AutoComplete_KeyUp(tmpcombo, e)
    'End Sub


    'Private Sub AutoComplete_KeyUp(ByVal cbo As ComboBox, ByVal e As KeyEventArgs)
    '    Dim sTypedText As String
    '    Dim iFoundIndex As Integer
    '    Dim sFoundText As String
    '    Dim sAppendText As String
    '    Select Case e.KeyCode
    '        Case Keys.Back, Keys.Left, Keys.Right, Keys.Up, Keys.Delete, Keys.Down, Keys.Home, Keys.End, Keys.ShiftKey, Keys.ControlKey
    '            Return
    '    End Select

    '    sTypedText = cbo.Text
    '    iFoundIndex = cbo.FindString(sTypedText)

    '    If iFoundIndex >= 0 Then
    '        sFoundText = cbo.Items(iFoundIndex)
    '        sAppendText = sFoundText.Substring(sTypedText.Length)
    '        cbo.Text = sTypedText & sAppendText
    '        cbo.SelectionStart = sTypedText.Length
    '        cbo.SelectionLength = sAppendText.Length
    '    End If
    'End Sub



End Class

