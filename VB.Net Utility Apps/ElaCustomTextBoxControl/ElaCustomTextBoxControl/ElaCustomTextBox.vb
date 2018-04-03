Imports System.Drawing
Imports System.Runtime.InteropServices
Imports System.Windows.Forms

Public Class ElaCustomTextBox
    Inherits TextBox

    Public Const WM_NCPAINT As Integer = &H85

    Private mCurrentBorderColor As Color
    Private mBorderColor As Color
    Private mBorderColorFocus As Color
    Private mBorderColorMouseEnter As Color
    Private mBorderThickness As BorderThicknessEnum

    Enum BorderThicknessEnum
        Normal = 2
        Thick = 4
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

    Public Sub New()
        MyBase.New()

        'This call is required by the Windows Form Designer.
        InitializeComponent()

        'Add any initialization after the InitializeComponent() call
        Me.BorderColor = Color.DeepSkyBlue
        Me.BorderColorFocus = Color.Orange
        Me.BorderColorMouseEnter = Color.Green
        Me.BorderThickness = BorderThicknessEnum.Thick

        mCurrentBorderColor = Me.mBorderColor
    End Sub

    Protected Overrides Sub OnResize(e As System.EventArgs)
        MyBase.OnResize(e)
        RedrawWindow(Me.Handle, IntPtr.Zero, IntPtr.Zero, RedrawWindowFlags.Frame Or RedrawWindowFlags.UpdateNow Or RedrawWindowFlags.Invalidate)
    End Sub

    Protected Overrides Sub WndProc(ByRef m As Message)
        MyBase.WndProc(m)

        If m.Msg = WM_NCPAINT Then
            Dim hDC As IntPtr = GetWindowDC(m.HWnd)
            Using g As Graphics = Graphics.FromHdc(hDC)

                g.DrawRectangle(New Pen(Me.mCurrentBorderColor, Me.BorderThickness), New Rectangle(0, 0, Me.Width, Me.Height))

            End Using
            ReleaseDC(m.HWnd, hDC)
        End If

    End Sub

    Protected Overrides Sub OnMouseEnter(ByVal e As System.EventArgs)
        MyBase.OnMouseEnter(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColorMouseEnter
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseLeave(ByVal e As System.EventArgs)
        MyBase.OnMouseLeave(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnGotFocus(ByVal e As System.EventArgs)
        MyBase.OnGotFocus(e)
        Me.mCurrentBorderColor = Me.BorderColorFocus
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnLostFocus(ByVal e As System.EventArgs)
        MyBase.OnLostFocus(e)
        Me.mCurrentBorderColor = Me.BorderColor
        Me.Invalidate()
    End Sub

    Protected Overrides Sub OnMouseHover(ByVal e As System.EventArgs)
        MyBase.OnMouseHover(e)
        If Me.Focused Then Exit Sub
        Me.mCurrentBorderColor = Me.BorderColorMouseEnter
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

End Class
