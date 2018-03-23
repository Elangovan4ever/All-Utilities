Imports System.Threading
Imports NLog

Public Class Form1

    Dim log As Logger = LogManager.GetCurrentClassLogger()

    Dim colorsArray() As Color = {Color.AliceBlue, Color.AntiqueWhite, Color.Aqua, Color.Aquamarine, Color.Azure, Color.Beige, Color.Bisque, Color.Black, Color.BlanchedAlmond, Color.Blue, Color.BlueViolet, Color.Brown, Color.BurlyWood, Color.CadetBlue, Color.Chartreuse, Color.Chocolate, Color.Coral, Color.CornflowerBlue, Color.Cornsilk, Color.Crimson, Color.Cyan, Color.DarkBlue, Color.DarkCyan, Color.DarkGoldenrod, Color.DarkGray, Color.DarkGreen, Color.DarkKhaki, Color.DarkMagenta, Color.DarkOliveGreen, Color.DarkOrange, Color.DarkOrchid, Color.DarkRed, Color.DarkSalmon, Color.DarkSeaGreen, Color.DarkSlateBlue, Color.DarkSlateGray, Color.DarkTurquoise, Color.DarkViolet, Color.DeepPink, Color.DeepSkyBlue, Color.DimGray, Color.DodgerBlue, Color.Firebrick, Color.FloralWhite, Color.ForestGreen, Color.Fuchsia, Color.Gainsboro, Color.GhostWhite, Color.Gold, Color.Goldenrod, Color.Gray, Color.Green, Color.GreenYellow, Color.Honeydew, Color.HotPink, Color.IndianRed, Color.Indigo, Color.Ivory, Color.Khaki, Color.Lavender, Color.LavenderBlush, Color.LawnGreen, Color.LemonChiffon, Color.LightBlue, Color.LightCoral, Color.LightCyan, Color.LightGoldenrodYellow, Color.LightGray, Color.LightGreen, Color.LightPink, Color.LightSalmon, Color.LightSeaGreen, Color.LightSkyBlue, Color.LightSlateGray, Color.LightSteelBlue, Color.LightYellow, Color.Lime, Color.LimeGreen, Color.Linen, Color.Magenta, Color.Maroon, Color.MediumAquamarine, Color.MediumBlue, Color.MediumOrchid, Color.MediumPurple, Color.MediumSeaGreen, Color.MediumSlateBlue, Color.MediumSpringGreen, Color.MediumTurquoise, Color.MediumVioletRed, Color.MidnightBlue, Color.MintCream, Color.MistyRose, Color.Moccasin, Color.NavajoWhite, Color.Navy, Color.OldLace, Color.Olive, Color.OliveDrab, Color.Orange, Color.OrangeRed, Color.Orchid, Color.PaleGoldenrod, Color.PaleGreen, Color.PaleTurquoise, Color.PaleVioletRed, Color.PapayaWhip, Color.PeachPuff, Color.Peru, Color.Pink, Color.Plum, Color.PowderBlue, Color.Purple, Color.Red, Color.RosyBrown, Color.RoyalBlue, Color.SaddleBrown, Color.Salmon, Color.SandyBrown, Color.SeaGreen, Color.SeaShell, Color.Sienna, Color.Silver, Color.SkyBlue, Color.SlateBlue, Color.SlateGray, Color.Snow, Color.SpringGreen, Color.SteelBlue, Color.Tan, Color.Teal, Color.Thistle, Color.Tomato, Color.Transparent, Color.Turquoise, Color.Violet, Color.Wheat, Color.White, Color.WhiteSmoke, Color.Yellow, Color.YellowGreen}

    Private Sub printColors()

        Dim myGraphics As Graphics
        Dim myRectangle As Rectangle
        myGraphics = Graphics.FromHwnd(ActiveForm().Handle)

        Dim leftPos As Integer = 10
        Dim x As Integer = leftPos
        Dim y As Integer = 50
        Dim width As Integer = 200
        Dim height As Integer = 20

        Dim xgap = 50
        Dim ygap = 10
        Dim screenAvailWidth As Integer = 1500

        For Each color In colorsArray
            Dim myPen As New Pen(color)
            myRectangle = New Rectangle(x, y, width, height)
            myGraphics.DrawRectangle(pen:=myPen, rect:=myRectangle)
            ''myGraphics.DrawRectangle(pen:=myPen, rect:=myRectangle)
            myGraphics.DrawString(color.Name, New Font("Arial", 10), New SolidBrush(Color.Black), myRectangle, New StringFormat)

            log.Debug("AfterDraw: color: " + color.ToString + ", x: " + x.ToString + ", y=" + y.ToString)

            If x + xgap + width > screenAvailWidth Then
                y += ygap + height
                x = leftPos
                log.Debug("Moving to next line")
            Else
                x += xgap + width
            End If

            ''x += If(x > leftPos, xgap + width, width)

            log.Debug("AfterCalc: color: " + color.ToString + ", x: " + x.ToString + ", y=" + y.ToString)
        Next
    End Sub

    Private Sub Form1_Load(sender As Object, e As EventArgs) Handles MyBase.Load
        Dim thread As Thread = New Thread(AddressOf paintColorsInThread)
        thread.IsBackground = True
        thread.Start()
    End Sub

    Sub paintColorsInThread()
        Dim printColorsInvoker As New printColorsDelegate(AddressOf Me.printColors)
        Me.BeginInvoke(printColorsInvoker)
    End Sub

    Delegate Sub printColorsDelegate()

    Private Sub Button1_Click(sender As Object, e As EventArgs) Handles Button1.Click
        Dim thread As Thread = New Thread(AddressOf paintColorsInThread)
        thread.IsBackground = True
        thread.Start()
    End Sub
End Class
