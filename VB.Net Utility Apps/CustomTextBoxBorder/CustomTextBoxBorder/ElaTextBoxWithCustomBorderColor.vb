
Imports System.Runtime.InteropServices

Public Class ElaTextBoxWithCustomBorderColor
    Inherits TextBox

    Public Const WM_NCPAINT As Integer = &H85

    Private mBorderColor As Color
    Private mBorderColorFocused As Color

    Public Property BorderColor As Color
        Get
            Return mBorderColor
        End Get
        Set(ByVal Value As Color)
            mBorderColor = Value
        End Set
    End Property

    Public Property BorderColorFocused As Color
        Get
            Return mBorderColorFocused
        End Get
        Set(ByVal Value As Color)
            mBorderColorFocused = Value
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
        MyBase.BorderStyle = Windows.Forms.BorderStyle.Fixed3D
        Me.BorderColor = Color.DeepSkyBlue
        Me.BorderColorFocused = Color.Orange
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
                If Me.Focused Then
                    g.DrawRectangle(New Pen(Me.BorderColorFocused), New Rectangle(0, 0, Me.Width - 1, Me.Height - 1))
                Else
                    g.DrawRectangle(New Pen(Me.BorderColor), New Rectangle(0, 0, Me.Width - 1, Me.Height - 1))
                End If
                g.DrawRectangle(SystemPens.Window, New Rectangle(1, 1, Me.Width - 3, Me.Height - 3))
            End Using
            ReleaseDC(m.HWnd, hDC)
        End If

    End Sub
End Class